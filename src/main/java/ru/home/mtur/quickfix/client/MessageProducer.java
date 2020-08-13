package ru.home.mtur.quickfix.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.home.mtur.quickfix.model.MsgHolder;
import ru.home.mtur.quickfix.utils.FixUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static ru.home.mtur.quickfix.utils.ConcurrentUtils.createNamedPool;
import static ru.home.mtur.quickfix.utils.ConcurrentUtils.shutdownThreadPool;

public class MessageProducer implements SessionStateListener {
    final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    private final int SENDER_THREADS = 1;
    private final int MSG_GEN_THREADS = 1;
    private final int MSG_DELAY_MS = 100;
    private final int MSG_NOTSENT_DELAY_MS = 5_000;
    private final int SESSION_NOT_READY_TIMEOUT = 1_000;
    private final int MSG_QUEUE_CAPACITY = 10_000;

    private LinkedBlockingQueue<MsgHolder> sendingQueue;
    private ExecutorService senderPool;
    private ExecutorService genPool;
    private ScheduledExecutorService periodicPool;

    private SessionID sessionID;
    private volatile boolean sessionIsActive = false;
    private volatile boolean isActive = false;
    private AtomicLong nextMsgID = new AtomicLong();

    public MessageProducer(SessionID sessionID) {
        this.sessionID = sessionID;

        sendingQueue = new LinkedBlockingQueue<>(MSG_QUEUE_CAPACITY);

        log.info("Creating Message producer of {} gen threads and {} sender threads.", MSG_GEN_THREADS, SENDER_THREADS);

        senderPool = createNamedPool("Sender", SENDER_THREADS);
        genPool = createNamedPool("Gen", MSG_GEN_THREADS);
        periodicPool = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Scheduled"));
    }

    public void start() {
        isActive = true;

        periodicPool.scheduleWithFixedDelay(() -> {
            log.info("Sending queue size: {}, remaining: {}", getQueueSize(), sendingQueue.remainingCapacity());
        }, 0, 1000, TimeUnit.MILLISECONDS);

        startSenders();
        startMsgGenerators();

        log.info("Message producer has been [started]");
    }

    private void startSenders() {
        for (int i = 0; i < SENDER_THREADS; i++) {
            senderPool.submit(() -> {
                String name = Thread.currentThread().getName();
                log.info("{} has been [started].", name);
                while (isActive)  {
                    try {
                        if (!sessionIsActive) {
                            TimeUnit.MILLISECONDS.sleep(SESSION_NOT_READY_TIMEOUT);
                            continue;
                        }

//                        log.info("Waiting for some message from the queue for sending ...");
                        MsgHolder msg = sendingQueue.take();
                        if (!sendMessage(msg)) {
                            log.warn("Message ID={} was not sent. Return to the queue and wait for {} ms", msg.getMsgId(), MSG_NOTSENT_DELAY_MS);
                            sendingQueue.offer(msg);
                            TimeUnit.MILLISECONDS.sleep(MSG_NOTSENT_DELAY_MS);
                        }

//                        TimeUnit.MILLISECONDS.sleep(MSG_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                log.info("{} has been [stopped].", name);
            });
        }
    }

    private void startMsgGenerators() {
        for (int i = 0; i < MSG_GEN_THREADS; i++) {
            genPool.submit(() -> {
                String name = Thread.currentThread().getName();
                log.info("{} has been [started].", name);
                while (isActive)  {
                    try {
                        MsgHolder msg = generateMessage(sessionID);
                        sendingQueue.offer(msg, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                        log.info("Added msg {} to the queue.", msg.getMsgId());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                log.info("{} has been [stopped].", name);
            });
        }
    }

    public void stop() {
        log.info("Stopping Message producer");
        isActive = false;

        shutdownThreadPool(senderPool, "Senders pool");
        shutdownThreadPool(periodicPool, "Scheduling pool");

        log.info("Message producer has been [stopped].");
    }

    public int getQueueSize() {
        return sendingQueue.size();
    }

    private MsgHolder generateMessage(SessionID sessionID) {
        long msgID = nextMsgID.getAndIncrement();
        Message message = FixUtils.generateOrder(msgID);
        return new MsgHolder(sessionID, message);
    }

    private boolean sendMessage(MsgHolder msgHolder) {
        try {
            SessionID sessionID = msgHolder.getSessionID();
            return Session.sendToTarget(msgHolder.getMsg(), sessionID);
        } catch (SessionNotFound e) {
            log.info("Session {} not found", sessionID, e);
            return false;
        }
    }

    @Override
    public void onConnect() {
        log.info("Session [{}] connected.", sessionID);

    }

    @Override
    public void onDisconnect() {
        sessionIsActive = false;
        log.info("Session [{}] disconnected.", sessionID);
    }

    @Override
    public void onLogon() {
        log.info("Session [{}] logged on.", sessionID);
        sessionIsActive = true;
    }

    @Override
    public void onLogout() {
        sessionIsActive = false;
        log.info("Session [{}] logged out.", sessionID);
    }

    @Override
    public void onReset() {
        log.info("Session [{}] reset.", sessionID);
    }

    @Override
    public void onRefresh() {
        log.info("Session [{}] refreshed.", sessionID);
    }

    @Override
    public void onMissedHeartBeat() {
        log.warn("Session [{}] has missing heartbeat.", sessionID);
    }

    @Override
    public void onHeartBeatTimeout() {
        log.warn("Session [{}] heartbeat timeout.", sessionID);
    }
}
