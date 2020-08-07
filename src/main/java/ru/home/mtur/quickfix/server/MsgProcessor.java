package ru.home.mtur.quickfix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.SessionID;
import ru.home.mtur.quickfix.common.MsgHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MsgProcessor {
    final Logger log = LoggerFactory.getLogger(MsgProcessor.class);

    private final int WORKER_THREAD_NUM = 1;
    private final long SHUTDOWN_TIMEOUT_MS = 10_000;

    private LinkedBlockingQueue<MsgHolder> queue;
    private ExecutorService pool;
    private volatile boolean isActive = false;

    public MsgProcessor() {
        queue = new LinkedBlockingQueue<>();
        pool = Executors.newFixedThreadPool(WORKER_THREAD_NUM, new ThreadFactory() {
            private final AtomicInteger nextId = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MsgProcessorWorker-" + nextId);
            }
        });
    }

    public void offerMessage(SessionID sessionID, Message msg) {
        MsgHolder holder = new MsgHolder(sessionID, msg);
        boolean offered = queue.offer(holder);
        if (!offered) {
            throw new IllegalStateException("Unable to offer msg to the queue.");
        }
    }

    public void start() {
        isActive = true;

        while (isActive)  {
            try {
                log.info("Waiting for some message from the queue for processing ...");
                MsgHolder msg = queue.take();
                processMessage(msg);
            } catch (InterruptedException e) {
                log.warn("Interrupted ...", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        log.info("Stopping MsgProcessor");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(SHUTDOWN_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    log.error("Not all submitted tasks were terminated within {} ms.", SHUTDOWN_TIMEOUT_MS);
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            isActive = false;
            log.info("MsgProcessor has been stopped.");
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    private void processMessage(MsgHolder msg) throws InterruptedException {
        log.info("Processing message: msgID: {}, session: {}, seqNum: {}", msg.getMsgId(), msg.getSessionID(), msg.getMsgSeq());
        TimeUnit.MICROSECONDS.sleep(500);
    }
}
