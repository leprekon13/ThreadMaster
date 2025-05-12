package org.example;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(
            2,                // corePoolSize
            4,                // maxPoolSize
            5,                // keepAliveTime
            TimeUnit.SECONDS, // timeUnit
            5,                // queueSize
            2,                // minSpareThreads
            2                 // numberOfQueues
        );

        for (int i = 0; i < 10; i++) {
            int taskId = i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " - Выполнение задачи: " + taskId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(Thread.currentThread().getName() + " - Завершение задачи: " + taskId);
            });
        }

        executor.shutdown();
    }
}