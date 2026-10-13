package com.kob.botrunningsystem.Utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

//沙箱子进程的入口。进程启动后阻塞在标准输入上等任务，执行完一个Bot就退出，不复用
public class BotRunner {

    public static final String BOT_CLASS = "com.kob.botrunningsystem.Utils.Bot";

    public static final String ERROR_PREFIX = "ERR ";

    public static final int READY = 'R';

    public static void main(String[] args) throws Exception {
        System.out.write(READY);//告诉主服务JVM已经启动完，可以接任务了
        System.out.flush();

        DataInputStream in = new DataInputStream(System.in);
        String botDir, input;
        try {
            botDir = in.readUTF();
            input = in.readUTF();
        } catch (EOFException e) {//主服务退出了
            return;
        }

        //Bot自己的输出一律丢弃，标准输出只用来回传结果
        PrintStream out = System.out;
        PrintStream nowhere = new PrintStream(OutputStream.nullOutputStream());
        System.setOut(nowhere);
        System.setErr(nowhere);

        try {
            //Bot的类从单独的目录加载，安全策略里这个目录没有任何权限
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{Path.of(botDir).toUri().toURL()},
                    BotRunner.class.getClassLoader());
            BotInterface bot = (BotInterface) loader.loadClass(BOT_CLASS).getConstructor().newInstance();
            out.println(bot.nextMove(input));
        } catch (Throwable e) {
            out.println(ERROR_PREFIX + e);
        }
        out.flush();
    }
}
