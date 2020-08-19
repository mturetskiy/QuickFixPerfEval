package ru.home.mtur.quickfix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

public class ServerApplication implements ApplicationExtended {
    final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    private MessageProcessor processor;

    public ServerApplication(MessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public boolean canLogon(SessionID sessionID) {
        return true;
    }

    @Override
    public void onBeforeSessionReset(SessionID sessionID) {
        log.info("onBeforeSessionReset for : {}", sessionID);
    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session: {} is creating", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("Session: {} is logging on", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Session: {} is logging out", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {

    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
//        log.info("Incoming message at sessionID: {}, msg: {}", sessionId, message);
        processor.offerMessage(sessionId, message);
    }
}
