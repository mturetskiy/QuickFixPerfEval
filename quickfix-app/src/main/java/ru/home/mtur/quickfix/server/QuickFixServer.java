package ru.home.mtur.quickfix.server;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import ru.home.mtur.quickfix.store.KafkaStoreFactory;
import ru.home.mtur.quickfix.utils.ConfigUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Properties;

import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

public class QuickFixServer {
    final static Logger log = LoggerFactory.getLogger(QuickFixServer.class);

    public static final String DEFAULT_CONFIG_NAME = "/server-sessions.settings";
    private String DEFAULT_KAFKA_PROPS_NAME = "/kafka-store.properties";

    private MessageProcessor processor;
    private AbstractSocketAcceptor acceptor;
    private KafkaStoreFactory storeFactory;

    public QuickFixServer(String[] args) throws ConfigError, FieldConvertError {
        String configFileName = ConfigUtils.getAppConfig(args, DEFAULT_CONFIG_NAME);
        log.info("Using config: {}", configFileName);

        this.processor = new MessageProcessor();
        Application application = new ServerApplication(processor);

        SessionSettings settings = ConfigUtils.loadSessionSettings(configFileName);
        Properties kafkaClientProps = ConfigUtils.loadProperties(DEFAULT_KAFKA_PROPS_NAME);

        MessageStoreFactory storeFactory = new MemoryStoreFactory();
//        MessageStoreFactory storeFactory = new JdbcStoreFactory(settings);
//        storeFactory = new KafkaStoreFactory(settings, kafkaClientProps);
        LogFactory logFactory = new SLF4JLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        String host = settings.getString(SETTING_SOCKET_ACCEPT_ADDRESS);
        int port = settings.getInt(SETTING_SOCKET_ACCEPT_PORT);

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        log.info("Configuring templated acceptor at: {}", socketAddress);

        SessionID templateSessionId = new SessionID("FIX.4.4", "QF_SRV", "*");

        acceptor = SocketAcceptor.newBuilder()
                .withApplication(application)
                .withLogFactory(logFactory)
                .withMessageFactory(messageFactory)
                .withMessageStoreFactory(storeFactory)
                .withSettings(settings)
                .build();

        acceptor.setSessionProvider(socketAddress, new DynamicAcceptorSessionProvider(settings, templateSessionId,
                application, storeFactory, logFactory, messageFactory));

        log.info("QuickFix server configured.");
    }

    public static void main(String[] args) throws ConfigError, FieldConvertError {
        final QuickFixServer server = new QuickFixServer(args);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }

    public void start() throws ConfigError {
        log.info("Starting QuickFixServer ...");

        processor.start();
        acceptor.start();

        log.info("Started QuickFixServer ...");
    }

    public void stop() {
        log.info("Stopping QuickFixServer ...");

        acceptor.stop();
        storeFactory.stop();
        processor.stop();

        log.info("Stopped QuickFixServer ...");
    }
}
