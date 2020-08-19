package ru.home.mtur.quickfix.store.bob;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SessionInfoSerializer implements Serializer<SessionInfoRecord> {
    final Logger log = LoggerFactory.getLogger(SessionInfoSerializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, SessionInfoRecord sessionInfoRecord) {
        return new byte[0];
    }

    @Override
    public byte[] serialize(String topic, Headers headers, SessionInfoRecord data) {
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
