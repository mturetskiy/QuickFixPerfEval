package ru.home.mtur.quickfix.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.util.Properties;

public class KafkaStoreFactory implements MessageStoreFactory {
    final Logger log = LoggerFactory.getLogger(KafkaStoreFactory.class);

    private final SessionSettings settings;
    private final Properties kafkaProps;

    public KafkaStoreFactory(SessionSettings settings, Properties kafkaProps) {
        this.settings = settings;
        this.kafkaProps = kafkaProps;
    }

    @Override
    public MessageStore create(SessionID sessionID) {
        return new KakfaStore(sessionID, settings, kafkaProps);
    }

    public SessionSettings getSettings() {
        return settings;
    }
}
