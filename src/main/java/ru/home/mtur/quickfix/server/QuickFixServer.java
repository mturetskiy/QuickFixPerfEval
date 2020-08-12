package ru.home.mtur.quickfix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.mina.acceptor.AbstractSocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

public class QuickFixServer {
    final static Logger log = LoggerFactory.getLogger(QuickFixServer.class);

    private QFApplication application;
    private MsgProcessor processor;
    private AbstractSocketAcceptor acceptor;

    public QuickFixServer(String configFileName) throws ConfigError, FieldConvertError {
        this.processor = new MsgProcessor();
        this.application = new QFApplication(processor);


        InputStream configFileResource = this.getClass().getResourceAsStream(configFileName);
        SessionSettings settings = new SessionSettings(configFileResource);
        MessageStoreFactory storeFactory = new MemoryStoreFactory();
        LogFactory logFactory = new SLF4JLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();

        String host = settings.getString(SETTING_SOCKET_ACCEPT_ADDRESS);
        Integer port = settings.getInt(SETTING_SOCKET_ACCEPT_PORT);

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        log.info("Configuring templated acceptor at: {}", socketAddress);

        SessionID templateSessionId = new SessionID("FIX.4.4", "SomeClient", "Server");
        acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);
        acceptor.setSessionProvider(socketAddress, new DynamicAcceptorSessionProvider(settings, templateSessionId,
                application, storeFactory, logFactory, messageFactory));

        log.info("QuickFix server configured.");
    }

    public static void main(String[] args) throws ConfigError, FieldConvertError {
        if (args.length != 1) {
            log.error("One argument with quickFix session settings config is required.");
            return;
        }

        String configFileName = args[0];
        log.info("Starting QuickFix server with config: {}", configFileName);

        final QuickFixServer server = new QuickFixServer(configFileName);
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
        processor.stop();

        log.info("Stopped QuickFixServer ...");
    }
}
