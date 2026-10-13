package com.kob.botrunningsystem.Utils;

import java.nio.charset.StandardCharsets;

//子进程的入口：由Consumer在独立的JVM中启动，用来执行用户的Bot代码
public class BotRunner {

    public static final String RESULT_PREFIX = "KOB_MOVE:";

    public static void main(String[] args) throws Exception {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        BotInterface bot = (BotInterface) Class.forName("com.kob.botrunningsystem.Utils.Bot")
                .getConstructor()
                .newInstance();
        System.out.println(RESULT_PREFIX + bot.nextMove(input));
    }
}
