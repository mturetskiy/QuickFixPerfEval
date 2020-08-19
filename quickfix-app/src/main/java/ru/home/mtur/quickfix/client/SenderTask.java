package ru.home.mtur.quickfix.client;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import ru.home.mtur.quickfix.model.MsgHolder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SenderTask implements Runnable {
    final Logger log = LoggerFactory.getLogger(SenderTask.class);

    private final int MSG_NOTSENT_DELAY_MS = 5_000;
    private final int SESSION_NOT_READY_TIMEOUT = 1_000;

    private LinkedBlockingQueue<MsgHolder> sendingQueue;

    private volatile boolean active = true;
    private volatile boolean sessionActive = false;

    private MeterRegistry registry;
    private Timer sendTimer;
    private Timer waitTimer;
    private Timer totalTimer;
    private Counter sentMessages;

    private long lastMeasuredTimePoint;
    private long lastMeasuredSentMsgCount;

    public SenderTask(LinkedBlockingQueue<MsgHolder> sendingQueue) {
        this.sendingQueue = sendingQueue;
        this.registry = new SimpleMeterRegistry();

        sentMessages = registry.counter("sentMessages");

        sendTimer = Timer.builder("sendingTime")
                .percentilePrecision(2)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        waitTimer = Timer.builder("waitingTime")
                .percentilePrecision(2)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
        totalTimer = Timer.builder("totalTime")
                .percentilePrecision(2)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        log.info("{} has been [started].", name);

        lastMeasuredTimePoint = System.currentTimeMillis();

        while (active)  {
            try {
                if (!sessionActive) {
                    TimeUnit.MILLISECONDS.sleep(SESSION_NOT_READY_TIMEOUT);
                    continue;
                }
                Timer.Sample start = Timer.start(registry);
                MsgHolder msg = waitTimer.record(this::obtainMessage);
                boolean isMsgSent = sendTimer.record(() -> sendMessage(msg));
                start.stop(totalTimer);
                sentMessages.increment();
                if (!isMsgSent && msg != null) {
                    log.warn("Message ID={} was not sent. Return to the queue and wait for {} ms", msg.getMsgId(), MSG_NOTSENT_DELAY_MS);
                    sendingQueue.offer(msg);
                    TimeUnit.MILLISECONDS.sleep(MSG_NOTSENT_DELAY_MS);
                }

                if (sentMessages.count() % 100_000 == 0) {
                    long currentTimePoint = System.currentTimeMillis();
                    long elapsedTimeMs = currentTimePoint - lastMeasuredTimePoint;
                    lastMeasuredTimePoint = currentTimePoint;

                    long currentSentMsgCount = (long) sentMessages.count();
                    long processed = currentSentMsgCount - lastMeasuredSentMsgCount;
                    lastMeasuredSentMsgCount = currentSentMsgCount;



                    double sendingRateRaw = (double) processed / elapsedTimeMs * 1000.0;
                    double elapsedTimeMeter = totalTimer.totalTime(SECONDS);
                    double sendingRateMeter = totalTimer.count() / elapsedTimeMeter;

                    log.info("Elapsed raw {} vs meter {}. Count: {} vs {}",
                            elapsedTimeMs / 1000.0, elapsedTimeMeter,
                            processed, totalTimer.count());

                    ValueAtPercentile[] wP = waitTimer.takeSnapshot().percentileValues();
                    ValueAtPercentile[] sP = sendTimer.takeSnapshot().percentileValues();

                    log.info("[iter={}/msgId={}, {} m/s or {} m/s] Sending stats2 (max/mean - mks). Percentiles: 50, 95, 99" +
                                    "\n\tWaitTime: {} / {}.  [{}, {}, {}]" +
                                    "\n\tSendTime: {} / {}.  [{}, {}, {}]",
                            sentMessages.count(), msg.getMsgId(),
                            sendingRateMeter,
                            sendingRateRaw,
                            waitTimer.max(MICROSECONDS), waitTimer.mean(MICROSECONDS), mks(wP[0]), mks(wP[1]), mks(wP[2]),
                            sendTimer.max(MICROSECONDS), sendTimer.mean(MICROSECONDS), mks(sP[0]), mks(sP[1]), mks(sP[2]));
                }

                SECONDS.sleep(5);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("{} has been [stopped].", name);
    }

    private MsgHolder obtainMessage() {
        try {
            return sendingQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private float mks(ValueAtPercentile pv) {
        return (float)(pv.value(MICROSECONDS));
    }

    private boolean sendMessage(MsgHolder msgHolder) {
        if (msgHolder == null) return false;
        SessionID sessionID = msgHolder.getSessionID();
        try {
            return Session.sendToTarget(msgHolder.getMsg(), sessionID);
        } catch (SessionNotFound e) {
            log.info("Session {} not found", sessionID, e);
            return false;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }
}
