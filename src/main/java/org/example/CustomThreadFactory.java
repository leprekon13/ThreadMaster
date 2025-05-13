package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private static final Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);

    private final AtomicInteger threadCount = new AtomicInteger(0);
    private final String poolName;

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = poolName + "-worker-" + threadCount.incrementAndGet();
        logger.info("[ThreadFactory] Создание нового потока: {}", threadName);
        return new Thread(r, threadName);
    }
}