package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPoolExecutor implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final BlockingQueue<Runnable>[] taskQueues;
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);
    private final AtomicInteger queueIndex = new AtomicInteger(0);
    private final ThreadFactory threadFactory;
    private volatile boolean isShutdown = false;

    @SuppressWarnings("unchecked")
    public CustomThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueSize, int minSpareThreads, int numQueues) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.taskQueues = new BlockingQueue[numQueues];
        for (int i = 0; i < numQueues; i++) {
            this.taskQueues[i] = new LinkedBlockingQueue<>(queueSize);
        }
        this.threadFactory = new CustomThreadFactory("MyPool");
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("Пул потоков закрыт. Новые задачи не принимаются.");
        }
        int index = queueIndex.getAndUpdate(i -> (i + 1) % taskQueues.length);
        if (!taskQueues[index].offer(command)) {
            handleTaskRejection(command);
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
        for (BlockingQueue<Runnable> queue : taskQueues) {
            queue.clear();
        }
    }

    private synchronized void manageThreads() {
        if (currentPoolSize.get() < corePoolSize
                || (currentPoolSize.get() < maxPoolSize && !allQueuesEmpty())
                || (currentPoolSize.get() - activeThreads() < minSpareThreads)) {
            Thread worker = threadFactory.newThread(this::workerTask);
            worker.start();
            currentPoolSize.incrementAndGet();
        }
    }

    private void workerTask() {
        try {
            while (!isShutdown || !allQueuesEmpty()) {
                Runnable task = pollFromQueues();
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

    private Runnable pollFromQueues() throws InterruptedException {
        for (BlockingQueue<Runnable> queue : taskQueues) {
            Runnable task = queue.poll(keepAliveTime, timeUnit);
            if (task != null) {
                return task;
            }
        }
        return null;
    }

    private boolean allQueuesEmpty() {
        for (BlockingQueue<Runnable> queue : taskQueues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int activeThreads() {
        return currentPoolSize.get() - (int) totalQueueCapacity();
    }

    private int totalQueueCapacity() {
        int totalCapacity = 0;
        for (BlockingQueue<Runnable> queue : taskQueues) {
            totalCapacity += queue.remainingCapacity();
        }
        return totalCapacity;
    }

    private void handleTaskRejection(Runnable command) {
        System.out.println("[Отклонено] Задача была отклонена из-за перегрузки!");
    }
}