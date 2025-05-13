package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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

        logger.info("Инициализирован пул потоков.");

        // Добавление задач в пул
        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            executor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                logger.info("{} - Выполнение задачи: {}", threadName, taskId);
                try {
                    Thread.sleep(2000); // Симуляция работы задачи
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("{} - Выполнение задачи {} прервано.", threadName, taskId);
                }
                logger.info("{} - Завершение задачи: {}", threadName, taskId);
            });
        }

        // Ожидание завершения запущенных задач
        try {
            Thread.sleep(15000); // Пауза для демонстрации работы пула
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Главный поток был прерван.");
        }

        // Завершение пула
        executor.shutdown();
        logger.info("Пул потоков отправлен на завершение.");
    }
}