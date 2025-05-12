package org.example;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(2, 4, 5, TimeUnit.SECONDS, 5);

        for (int i = 0; i < 10; i++) {
            int taskId = i;
            executor.execute(() -> {
                System.out.println("Выполнение задачи: " + taskId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Завершение задачи: " + taskId);
            });
        }

        executor.shutdown();
    }
}