package com.kob.botrunningsystem.Service.impl.utils;

import com.kob.botrunningsystem.Utils.BotRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class Consumer {

    private static RestTemplate restTemplate;

    private static String receiveBotMoveUrl;

    private static final String PACKAGE_DIR = "com/kob/botrunningsystem/Utils/";

    private static final String RUNNER_CLASS = "com.kob.botrunningsystem.Utils.BotRunner";

    //每份Bot代码编译一次，按代码的哈希值缓存在这个目录下
    private static final Path WORK_ROOT = Path.of(System.getProperty("java.io.tmpdir"), "kob-bots");

    private static final int MAX_OUTPUT_BYTES = 64 * 1024;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    @Value("${kob.backend.url}")
    public void setBackendUrl(String url) {
        Consumer.receiveBotMoveUrl = url + "/pk/receive/bot/move";
    }

    public void startTimeOut(long timeout, Bot bot) {
        try {
            Path dir = prepare(bot.getBotCode());
            Integer direction = run(dir, bot.getInput(), timeout);

            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("user_id", bot.getUserId().toString());
            data.add("direction", direction.toString());
            restTemplate.postForObject(receiveBotMoveUrl, data, String.class);

            System.out.println("move: " + bot.getUserId() + " " + direction);
        } catch (Exception e) {
            System.out.println("bot failed: " + bot.getUserId() + " " + e.getMessage());
        }
    }

    //编译Bot代码，返回可以直接作为classpath的目录
    private Path prepare(String code) throws Exception {
        byte[] source = code.getBytes(StandardCharsets.UTF_8);
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source));
        Path dir = WORK_ROOT.resolve(hash);
        Path packageDir = dir.resolve(PACKAGE_DIR);
        if (Files.exists(packageDir.resolve("Bot.class"))) {
            return dir;
        }

        Files.createDirectories(packageDir);
        copyClass(packageDir, "BotInterface.class");
        copyClass(packageDir, "BotRunner.class");
        Files.writeString(dir.resolve("bot.policy"), "grant {};");//不授予任何权限
        Path sourceFile = packageDir.resolve("Bot.java");
        Files.write(sourceFile, source);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("找不到Java编译器，请使用JDK运行");
        }
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int result = compiler.run(null, null, errors,
                "-proc:none", "-encoding", "UTF-8",
                "-cp", dir.toString(), "-d", dir.toString(),
                sourceFile.toString());
        if (result != 0) {
            throw new IllegalArgumentException("编译失败\n" + errors);
        }
        return dir;
    }

    private void copyClass(Path packageDir, String name) throws IOException {
        try (InputStream in = Consumer.class.getResourceAsStream("/" + PACKAGE_DIR + name)) {
            Files.write(packageDir.resolve(name), in.readAllBytes());
        }
    }

    //在独立的JVM中执行Bot，超时直接杀掉进程
    private Integer run(Path dir, String input, long timeout) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Xmx64m");
        command.add("-XX:+UseSerialGC");
        command.add("-XX:TieredStopAtLevel=1");
        if (Runtime.version().feature() < 24) {//Java 24起SecurityManager被移除，届时只剩进程级隔离
            command.add("-Djava.security.manager");
            command.add("-Djava.security.policy==" + dir.resolve("bot.policy").toUri());
        }
        command.add("-cp");
        command.add(dir.toString());
        command.add(RUNNER_CLASS);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(dir.toFile());
        builder.redirectErrorStream(true);
        String systemRoot = System.getenv("SystemRoot");
        builder.environment().clear();//不把服务的环境变量泄露给Bot
        if (systemRoot != null) {
            builder.environment().put("SystemRoot", systemRoot);
        }

        Process process = builder.start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream in = process.getInputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    synchronized (output) {
                        if (output.size() < MAX_OUTPUT_BYTES) {
                            output.write(buffer, 0, n);
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        });
        reader.setDaemon(true);
        reader.start();

        try (OutputStream out = process.getOutputStream()) {
            out.write(input.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {//子进程提前退出
        }

        if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("执行超时");
        }
        reader.join(1000);

        String text;
        synchronized (output) {
            text = output.toString(StandardCharsets.UTF_8);
        }
        String[] lines = text.split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].startsWith(BotRunner.RESULT_PREFIX)) {
                return Integer.parseInt(lines[i].substring(BotRunner.RESULT_PREFIX.length()).trim());
            }
        }
        throw new IllegalStateException("没有输出结果\n" + text);
    }
}
