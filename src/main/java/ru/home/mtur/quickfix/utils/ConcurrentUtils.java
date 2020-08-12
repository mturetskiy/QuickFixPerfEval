package ru.home.mtur.quickfix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtils {
    static final Logger log = LoggerFactory.getLogger(ConcurrentUtils.class);

    /*
    Suitable for thread pool with tasks that are endless (like while(active) do .. ). They won't
    stop at the end, so the only way to stop waiting for something (like next element in the blockign queue)
    is to interrupt the thread.
     */
    public static void shutdownThreadPool(ExecutorService pool, String name, long timeout) {
        log.info("Stopping pool: [{}]", name);
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                log.error("Not all submitted tasks were terminated within {} ms.", timeout);
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Pool [{}] has been stopped.", name);
    }
}
