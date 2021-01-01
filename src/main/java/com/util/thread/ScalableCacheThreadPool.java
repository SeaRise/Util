package com.util.thread;

import java.util.*;
import java.util.concurrent.*;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可以伸缩线程数目的线程池.
 * */
public class ScalableCacheThreadPool implements Executor {
    protected final Logger logger = LoggerFactory.getLogger(ScalableCacheThreadPool.class);

    private final Queue<Runnable> pendingQueue;

    private final int maxRunningNum;

    private int runningNum = 0;

    private final ExecutorService executor;

    private boolean isStop = false;

    public ScalableCacheThreadPool(String prefix, int maxRunningNum) {
        pendingQueue = new LinkedList<>();
        Preconditions.checkArgument(maxRunningNum > 0,
                String.format("maxRunningNum(%s) <= 0", maxRunningNum));
        this.maxRunningNum = maxRunningNum;

        this.executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat(prefix + "-%d").build());
    }

    private synchronized void refresh() {
        while (runningNum < maxRunningNum) {
            Runnable next = pendingQueue.poll();
            if (Objects.isNull(next)) {
                return;
            }
            runningNum++;
            executor.execute(newRunnable(next));
        }
    }

    // 为了复用调用reUseCurThread的线程，如果用refresh，在cacheThreadPool的机制下，这条线程会被回收.
    // 注意不能在synchronized下调用这个方法
    private Optional<Runnable> reUseCurThread() {
        Runnable nextFirst;
        synchronized (this) {
            if (runningNum >= maxRunningNum) {
                return Optional.empty();
            }

            nextFirst = pendingQueue.poll();
            if (Objects.isNull(nextFirst)) {
                return Optional.empty();
            }
            runningNum++;

            refresh();
        }
        return Optional.of(nextFirst);
    }

    private Runnable newRunnable(Runnable runnable) {
        return () -> {
            doRun(runnable);

            // reUseCurThread().ifPresent(task1 -> newRunnable(task1).run());
            // 会造成oom, 因为jvm没有尾递归优化, 方法栈会一直增长.
            Optional<Runnable> next = reUseCurThread();
            while (next.isPresent()) {
                doRun(next.get());
                next = reUseCurThread();
            }
        };
    }

    private void doRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            logger.error("runnable occur error: " + e.getMessage());
        } finally {
            synchronized (this) {
                runningNum--;
            }
        }
    }

    @Override
    public void execute(Runnable runnable) {
        if (isStop) {
            throw new RuntimeException("ScalableCacheThreadPool had stop");
        }

        if (runningNum < maxRunningNum) {
            if (pendingQueue.isEmpty()) {
                runningNum++;
                executor.submit(newRunnable(runnable));
            } else {
                pendingQueue.add(runnable);
                refresh();
            }
        } else {
            pendingQueue.add(runnable);
        }
    }

    public synchronized void shutdown() {
        if (isStop) {
            return;
        }
        isStop = true;
        executor.shutdown();
    }

    public synchronized List<Runnable> shutdownNow() {
        if (isStop) {
            return ImmutableList.of();
        }
        isStop = true;
        pendingQueue.clear();
        return executor.shutdownNow();
    }

    public boolean isShutdown() {
        return isStop;
    }
}