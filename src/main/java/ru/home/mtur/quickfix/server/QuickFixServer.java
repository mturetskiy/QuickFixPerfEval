package ru.home.mtur.quickfix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionSettings;

import java.io.FileInputStream;

public class QuickFixServer {
    final Logger log = LoggerFactory.getLogger(QuickFixServer.class);

    private QFApplication application;
    private MsgProcessor processor;

    public QuickFixServer() {
        this.processor = new MsgProcessor();
        this.application = new QFApplication(processor);


        SessionSettings settings = new SessionSettings(new FileInputStream(fileName));
    }

    public static void main(String[] args) {
        final QuickFixServer server = new QuickFixServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }

    public void start() {
        log.info("Starting QuickFixServer ...");

        processor.start();

        log.info("Started QuickFixServer ...");
    }

    public void stop() {
        log.info("Stopping QuickFixServer ...");

        processor.stop();

        log.info("Stopped QuickFixServer ...");
    }
}
