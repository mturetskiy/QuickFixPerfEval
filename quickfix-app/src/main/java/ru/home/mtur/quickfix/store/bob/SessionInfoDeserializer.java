package ru.home.mtur.quickfix.store.bob;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SessionInfoDeserializer implements Deserializer<SessionInfoRecord> {
    final Logger log = LoggerFactory.getLogger(SessionInfoDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public SessionInfoRecord deserialize(String s, byte[] bytes) {
        return null;
    }

    @Override
    public SessionInfoRecord deserialize(String topic, Headers headers, byte[] data) {
        return null;
    }

    @Override
    public void close() {

    }
}
