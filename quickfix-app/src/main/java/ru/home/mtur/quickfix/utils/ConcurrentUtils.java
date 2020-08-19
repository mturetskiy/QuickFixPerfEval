package ru.home.mtur.quickfix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentUtils {
    static final Logger log = LoggerFactory.getLogger(ConcurrentUtils.class);

    public static final long SHUTDOWN_TIMEOUT_MS = 10_000;

    /*
    Suitable for thread pool with tasks that are endless (like while(active) do .. ). They won't
    stop at the end, so the only way to stop waiting for something (like next element in the blockign queue)
    is to interrupt the thread.
     */
    public static void shutdownThreadPool(ExecutorService pool, String name) {
        log.info("Stopping pool: [{}]", name);
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                log.error("Not all submitted tasks were terminated within {} ms.", SHUTDOWN_TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Pool [{}] has been stopped.", name);
    }

    public static ExecutorService createNamedPool(String threadName, int threadsNum) {
        return Executors.newFixedThreadPool(threadsNum, new ThreadFactory() {
            private final AtomicInteger nextId = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadName + "-" + nextId.getAndIncrement());
            }
        });
    }
}
