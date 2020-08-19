package ru.home.mtur.quickfix.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.MessageStore;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

public class KakfaStore implements MessageStore {
    final Logger log = LoggerFactory.getLogger(KakfaStore.class);

    private SessionID sessionID;
    private final SessionSettings settings;
    private final Properties kafkaProps;


    public KakfaStore(SessionID sessionID, SessionSettings settings, Properties kafkaProps) {
        this.sessionID = sessionID;
        this.settings = settings;
        this.kafkaProps = kafkaProps;
    }

    @Override
    public boolean set(int sequence, String message) {
        return false;
    }

    @Override
    public void get(int startSequence, int endSequence, Collection<String> messages) {

    }

    @Override
    public int getNextSenderMsgSeqNum() {
        return 0;
    }

    @Override
    public int getNextTargetMsgSeqNum() {
        return 0;
    }

    @Override
    public void setNextSenderMsgSeqNum(int next) {

    }

    @Override
    public void setNextTargetMsgSeqNum(int next) {

    }

    @Override
    public void incrNextSenderMsgSeqNum() throws IOException {

    }

    @Override
    public void incrNextTargetMsgSeqNum() throws IOException {

    }

    @Override
    public Date getCreationTime() throws IOException {
        return null;
    }

    @Override
    public void reset() throws IOException {

    }

    @Override
    public void refresh() throws IOException {

    }
}
