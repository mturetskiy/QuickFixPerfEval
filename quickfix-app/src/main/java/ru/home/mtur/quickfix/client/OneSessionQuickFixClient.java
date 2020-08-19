package ru.home.mtur.quickfix.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.home.mtur.quickfix.utils.ConfigUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static quickfix.SessionSettings.*;
import static ru.home.mtur.quickfix.utils.ConfigUtils.getDefaultSessionID;

public class OneSessionQuickFixClient {
    static final Logger log = LoggerFactory.getLogger(OneSessionQuickFixClient.class);
    private static AtomicInteger nextClientID = new AtomicInteger();

    private SocketInitiator initiator;
    private MessageProducer producer;
    private static CountDownLatch shutdownLatch = new CountDownLatch(1);
    private int clientID;
    private SessionID sessionID;

    public OneSessionQuickFixClient(String configFileName) throws ConfigError {
        this.clientID = nextClientID.incrementAndGet();
        SessionSettings sessionSettings = ConfigUtils.loadSessionSettings(configFileName);

        String senderCompID = sessionSettings.getString(SENDERCOMPID);
        sessionSettings.setString(SENDERCOMPID, senderCompID + "-" + clientID);



        sessionID = getDefaultSessionID(sessionSettings);

        sessionSettings.set(sessionID, new Dictionary());
        log.info("Using sessionId: {}", sessionID);

        initiator = SocketInitiator.newBuilder()
                .withApplication(new ClientApplication())
                .withLogFactory(new SLF4JLogFactory(sessionSettings))
                .withMessageFactory(new DefaultMessageFactory())
                .withMessageStoreFactory(new MemoryStoreFactory())
                .withSettings(sessionSettings)
                .build();

        producer = new MessageProducer(sessionID);
    }

    public static void main(String[] args) throws ConfigError, InterruptedException {
        if (args.length != 1) {
            log.error("One argument with quickFix session settings config is required.");
            return;
        }

        String configFileName = args[0];
        log.info("Starting QuickFix client with config: {}", configFileName);

        OneSessionQuickFixClient client = new OneSessionQuickFixClient(configFileName);
        Runtime.getRuntime().addShutdownHook(new Thread(client::stop));

        client.start();

        shutdownLatch.await();
    }

    public void start() throws ConfigError {
        log.info("Starting QuickFix client ...");

        initiator.start();
        List<Session> managedSessions = initiator.getManagedSessions();
        // Will work only because thee is only one session configured
        managedSessions.forEach(s -> s.addStateListener(producer));

        producer.start();

        log.info("Started QuickFix client.");
    }

    public void stop() {
        log.info("Stopping QuickFix client ...");

        producer.stop();
        initiator.stop();
        shutdownLatch.countDown();

        log.info("Stopped QuickFix client.");
    }
}
