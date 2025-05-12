package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutor implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);
    private final ThreadFactory threadFactory;
    private boolean isShutdown = false;

    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.threadFactory = Executors.defaultThreadFactory();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("Пул потоков закрыт. Новые задачи не принимаются.");
        }
        if (!taskQueue.offer(command)) {
            System.out.println("[Отклонено] Задача была отклонена из-за перегрузки!");
        } else {
            manageThreads();
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<>(callable);
        execute(task);
        return task;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        taskQueue.clear();
    }

    private synchronized void manageThreads() {
        if (currentPoolSize.get() < corePoolSize || (currentPoolSize.get() < maxPoolSize && !taskQueue.isEmpty())) {
            Thread worker = threadFactory.newThread(this::workerTask);
            worker.start();
            currentPoolSize.incrementAndGet();
        }
    }

    private void workerTask() {
        try {
            while (!isShutdown || !taskQueue.isEmpty()) {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    task.run();
                } else if (currentPoolSize.get() > corePoolSize) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            currentPoolSize.decrementAndGet();
        }
    }
}