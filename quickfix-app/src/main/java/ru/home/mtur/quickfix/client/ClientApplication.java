package ru.home.mtur.quickfix.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

public class ClientApplication implements ApplicationExtended {
    static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    @Override
    public boolean canLogon(SessionID sessionID) {
        return true;
    }

    @Override
    public void onBeforeSessionReset(SessionID sessionID) {
        log.info("onBeforeSessionReset for {}", sessionID);
    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("onCreate for {}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("onLogon for {}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("onLogout for {}", sessionId);
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
        log.info("Received response from server. Session: {}, msg: {}", sessionId, message);
    }
}
