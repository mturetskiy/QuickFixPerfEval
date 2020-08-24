package ru.home.mtur.quickfix.server;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.step.StepCounter;
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

    private MeterRegistry registry;
    private StepCounter processedCounter;

    private AtomicLong processedMsgCount = new AtomicLong();

    public MessageProcessor() {
        queue = new LinkedBlockingQueue<>();
        pool = createNamedPool("Worker", WORKER_THREAD_NUM);
        this.registry = new SimpleMeterRegistry();
        // This step counter rolled out every 1000 ms, so we need to call count() with the same rate, so prev counted value will be correct.
        processedCounter = new StepCounter(new Meter.Id("processedMessages", Tags.empty(), null, null, Meter.Type.COUNTER), registry.config().clock(), 1000);

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

        periodicPool.scheduleWithFixedDelay(() -> {
            log.info("Processed msgs: {}. Message processing rate: {} m/s. ", processedMsgCount, (float) processedCounter.count());
        }, 0, 1000, TimeUnit.MILLISECONDS);

        pool.submit(() -> {
            log.info("Msg processor worker has been [started].");
            while (isActive)  {
                try {
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
        processedCounter.increment();
        long currentMsgNum = processedMsgCount.getAndIncrement();
        if (currentMsgNum % 100_000 == 0) {
            log.info("Processing message: msgID: {}, session: {}, seqNum: {}", msg.getMsgId(), msg.getSessionID(), msg.getMsgSeq());
        }
    }
}
