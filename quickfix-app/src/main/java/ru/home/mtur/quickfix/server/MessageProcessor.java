package ru.home.mtur.quickfix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.SessionID;
import ru.home.mtur.quickfix.model.MsgHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static ru.home.mtur.quickfix.utils.ConcurrentUtils.createNamedPool;
import static ru.home.mtur.quickfix.utils.ConcurrentUtils.shutdownThreadPool;

public class MessageProcessor {
    final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    private final int WORKER_THREAD_NUM = 1;

    private LinkedBlockingQueue<MsgHolder> queue;
    private ExecutorService pool;
    private ScheduledExecutorService periodicPool;
    private volatile boolean isActive = false;

    private AtomicLong processedMsgCount = new AtomicLong();

    // For stats calc:
    private AtomicLong lastMeasureTimePoint = new AtomicLong();
    private AtomicLong lastProcessedMsgCount = new AtomicLong();

    public MessageProcessor() {
        queue = new LinkedBlockingQueue<>();
        pool = createNamedPool("Worker", WORKER_THREAD_NUM);
        periodicPool = Executors.newScheduledThreadPool(1, r -> new Thread(r, "MsgProcessor Scheduled"));
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
        lastMeasureTimePoint.set(System.currentTimeMillis());

        periodicPool.scheduleWithFixedDelay(() -> {
//            log.info("Incoming queue size: {}", getQueueSize());

            // Calc msg processing speed:
            
            long currentTimePoint = System.currentTimeMillis();
            long currentProcessedMsgCount = processedMsgCount.get();
            long prevTimePoint = lastMeasureTimePoint.getAndSet(currentTimePoint);
            long prevProcessedMsgCount = lastProcessedMsgCount.getAndSet(currentProcessedMsgCount);

            long elapsedTimeMs = currentTimePoint - prevTimePoint;
            long processed = currentProcessedMsgCount - prevProcessedMsgCount;
            double processingRate = (double) processed / elapsedTimeMs * 1000.0;

            log.info("Processed msgs: {}. Message processing rate: {} m/s", currentProcessedMsgCount, (float)processingRate);




        }, 0, 1000, TimeUnit.MILLISECONDS);

        pool.submit(() -> {
            log.info("Msg processor worker has been [started].");
            while (isActive)  {
                try {
//                    log.info("Waiting for some message from the queue for processing ...");
                    MsgHolder msg = queue.take();
                    processMessage(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log.info("{} has been [stopped].", Thread.currentThread().getName());
        });

        log.info("Message processor has been [started] with {} active workers.", WORKER_THREAD_NUM);
    }

    public void stop() {
        log.info("Stopping MsgProcessor");
        isActive = false;

        shutdownThreadPool(pool, "Workers pool");
        shutdownThreadPool(periodicPool, "Scheduling pool");

        log.info("MsgProcessor has been [stopped].");
    }

    public int getQueueSize() {
        return queue.size();
    }

    private void processMessage(MsgHolder msg) throws InterruptedException {
        long currentMsgNum = processedMsgCount.getAndIncrement();
        if (currentMsgNum % 10_000 == 0) {
            log.info("Processing message: msgID: {}, session: {}, seqNum: {}", msg.getMsgId(), msg.getSessionID(), msg.getMsgSeq());
        }
//        TimeUnit.MICROSECONDS.sleep(500);
    }
}
