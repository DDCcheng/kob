package com.kob.botrunningsystem.Service.impl.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BotPool {

    private static final int WORKERS = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors()));

    private static final long TIMEOUT = 500;//每一步的执行时限，单位毫秒

    private final Sandbox sandbox = new Sandbox(WORKERS);

    //工作线程只负责调度，Bot本身跑在沙箱子进程里，超时杀掉进程后线程立刻回到池中，
    //所以死循环的Bot最多占用一个线程500ms，占不满线程池。
    //队列满了以后由提交任务的请求线程自己执行（CallerRunsPolicy）：
    //不丢弃任何一步操作，同时让上游的请求变慢，形成反压。
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            WORKERS, WORKERS,
            0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(WORKERS * 2),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public void addBot(Integer userId, String botCode, String input) {
        Bot bot = new Bot(userId, botCode, input);
        executor.execute(() -> new Consumer().startTimeOut(sandbox, TIMEOUT, bot));
    }
}
