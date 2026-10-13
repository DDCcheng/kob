package com.kob.botrunningsystem.Service.impl.utils;

import com.kob.botrunningsystem.Utils.BotRunner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

//Bot沙箱：用户代码只在一次性的子进程里执行，和服务不共享内存
public class Sandbox {

    private static final String PACKAGE_DIR = "com/kob/botrunningsystem/Utils/";

    private static final long STARTUP_TIMEOUT = 5000;//等子进程JVM启动的时限，不计入Bot的执行时间

    private final Path runnerDir;//BotRunner和BotInterface，子进程的classpath

    private final Path botsDir;//每份Bot代码编译一次，按代码的哈希值分目录存放

    private final Path policyFile;

    private final Map<String, Path> compiled = new ConcurrentHashMap<>();

    //预先启动好、正在等任务的子进程，省掉每一步的JVM启动时间
    private final BlockingQueue<Process> idle;

    public Sandbox(int warmProcesses) {
        try {
            Path root = Files.createTempDirectory("kob-bots");
            runnerDir = Files.createDirectories(root.resolve("runner"));
            botsDir = Files.createDirectories(root.resolve("bots"));
            Path runnerPackage = Files.createDirectories(runnerDir.resolve(PACKAGE_DIR));
            copyClass(runnerPackage, "BotInterface.class");
            copyClass(runnerPackage, "BotRunner.class");

            //只有BotRunner所在的目录有权限，Bot的目录不在其中，所以Bot什么权限都没有
            policyFile = root.resolve("sandbox.policy");
            Files.writeString(policyFile,
                    "grant codeBase \"" + runnerDir.toUri() + "-\" { permission java.security.AllPermission; };");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanUp(root)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        idle = new ArrayBlockingQueue<>(warmProcesses);
        for (int i = 0; i < warmProcesses; i++) {
            refill();
        }
    }

    //执行一步Bot，返回它给出的方向。timeout只计Bot自己的执行时间，超时会直接杀掉子进程
    public Integer run(String code, String input, long timeout) throws Exception {
        Path botDir = compile(code);

        Process process = idle.poll();
        if (process == null || !process.isAlive()) {
            process = start();
        }
        try {
            awaitReady(process);
            try (DataOutputStream out = new DataOutputStream(process.getOutputStream())) {
                out.writeUTF(botDir.toString());
                out.writeUTF(input);
            }
            if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("执行超时");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (output.isEmpty() || output.startsWith(BotRunner.ERROR_PREFIX)) {
                throw new IllegalStateException("运行出错 " + output);
            }
            return Integer.parseInt(output);
        } finally {
            process.destroyForcibly();
            refill();
        }
    }

    //等子进程发来就绪信号。预热好的进程早就发过了，这里会立刻返回
    private void awaitReady(Process process) throws IOException, InterruptedException {
        InputStream in = process.getInputStream();
        long deadline = System.currentTimeMillis() + STARTUP_TIMEOUT;
        while (in.available() == 0) {
            if (!process.isAlive() || System.currentTimeMillis() > deadline) {
                throw new IllegalStateException("沙箱进程启动失败");
            }
            Thread.sleep(2);
        }
        if (in.read() != BotRunner.READY) {
            throw new IllegalStateException("沙箱进程启动失败");
        }
    }

    private Path compile(String code) {
        byte[] source = code.getBytes(StandardCharsets.UTF_8);
        //同一份代码并发到达时，computeIfAbsent保证只编译一次
        return compiled.computeIfAbsent(sha256(source), hash -> {
            try {
                Path botDir = Files.createDirectories(botsDir.resolve(hash));
                Path sourceFile = botDir.resolve("Bot.java");
                Files.write(sourceFile, source);

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) {
                    throw new IllegalStateException("找不到Java编译器，请使用JDK运行");
                }
                ByteArrayOutputStream errors = new ByteArrayOutputStream();
                int result = compiler.run(null, null, errors,
                        "-proc:none", "-encoding", "UTF-8",
                        "-cp", runnerDir.toString(), "-d", botDir.toString(),
                        sourceFile.toString());
                if (result != 0) {
                    throw new IllegalArgumentException("编译失败\n" + errors);
                }
                return botDir;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private Process start() throws IOException {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-Xmx64m");
        command.add("-XX:+UseSerialGC");
        command.add("-XX:TieredStopAtLevel=1");
        if (Runtime.version().feature() < 24) {//Java 24起SecurityManager被移除，届时只剩进程级隔离
            command.add("-Djava.security.manager");
            command.add("-Djava.security.policy==" + policyFile.toUri());
        }
        command.add("-cp");
        command.add(runnerDir.toString());
        command.add(BotRunner.class.getName());

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(runnerDir.toFile());
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        String systemRoot = System.getenv("SystemRoot");
        builder.environment().clear();//不把服务的环境变量泄露给Bot
        if (systemRoot != null) {
            builder.environment().put("SystemRoot", systemRoot);
        }
        return builder.start();
    }

    private void refill() {
        if (idle.remainingCapacity() == 0) return;
        try {
            Process process = start();
            if (!idle.offer(process)) {
                process.destroyForcibly();
            }
        } catch (IOException e) {
            System.out.println("sandbox process failed to start: " + e.getMessage());
        }
    }

    private void cleanUp(Path root) {
        idle.forEach(Process::destroyForcibly);
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException ignored) {
        }
    }

    private static void copyClass(Path packageDir, String name) throws IOException {
        try (InputStream in = Sandbox.class.getResourceAsStream("/" + PACKAGE_DIR + name)) {
            Files.write(packageDir.resolve(name), in.readAllBytes());
        }
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
