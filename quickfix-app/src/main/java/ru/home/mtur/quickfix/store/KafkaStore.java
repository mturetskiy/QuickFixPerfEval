package ru.home.mtur.quickfix.store;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.MessageStore;
import quickfix.SessionID;
import quickfix.SessionSettings;
import ru.home.mtur.quickfix.store.bob.SessionInMsgRecord;
import ru.home.mtur.quickfix.store.bob.SessionInfoDeserializer;
import ru.home.mtur.quickfix.store.bob.SessionInfoRecord;
import ru.home.mtur.quickfix.store.bob.SessionOutMsgRecord;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaStore implements MessageStore {
    final Logger log = LoggerFactory.getLogger(KafkaStore.class);

    private final String QF_TOPIC_PREFIX = "qf-session-";
    private final String KAFKA_CLIENT_PREFIX = "qf-store-";
    private final String QF_SESSIONINFO_TOPIC_SUFFIX = "-info";
    private final String QF_IN_TOPIC_SUFFIX = "-in";
    private final String QF_OUT_TOPIC_SUFFIX = "-out";

    private SessionID sessionID;
    private final SessionSettings settings;
    private final Properties kafkaProps;

    private String sessionTopicPrefix;

    private Producer<String, SessionInfoRecord> sessionInfoProducer;
    private Consumer<String, SessionInfoRecord> sessionInfoConsumer;

    private Producer<String, SessionInMsgRecord> inMsgProducer;
    private Consumer<String, SessionInMsgRecord> inMsgConsumer;

    private Producer<String, SessionOutMsgRecord> outMsgProducer;
    private Consumer<String, SessionOutMsgRecord> outMsgConsumer;

    private AdminClient adminClient;


    public KafkaStore(SessionID sessionID, SessionSettings settings, Properties kafkaProps) {
        this.sessionID = sessionID;
        this.settings = settings;
        this.kafkaProps = kafkaProps;

        this.sessionTopicPrefix = QF_TOPIC_PREFIX + sessionID.toString();

        init();
    }

    private void init() {
        // Ensure topics for given session exist:
        sessionInfoConsumer = createSessionInfoConsumer();
    }

    public void stop() {
        sessionInfoConsumer.close();

        log.info("Session info consumer has been stopped for : {}", sessionID);
    }

    private Consumer<String, SessionInfoRecord> createSessionInfoConsumer() {
        Properties adminProps = new Properties();
        adminProps.putAll(kafkaProps);
        adminProps.setProperty(AdminClientConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_PREFIX + "admin-client");
        adminProps.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
        adminClient = AdminClient.create(adminProps);

        Properties props = new Properties();
        props.putAll(kafkaProps);
        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_PREFIX + "session-info-consumer");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_CLIENT_PREFIX + "group");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SessionInfoDeserializer.class.getName());

        KafkaConsumer<String, SessionInfoRecord> consumer = new KafkaConsumer<>(props);


        log.info("Getting available topics: ");
        ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().timeoutMs(3000));
        try {
            Set<String> topics = listTopicsResult.names().get();
            topics.forEach(t -> log.info("Topic: {}", t));
            String sessionInfoTopic = sessionTopicPrefix + QF_SESSIONINFO_TOPIC_SUFFIX;
            if (!topics.contains(sessionInfoTopic)) {
                createTopic(sessionInfoTopic);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Unable to collect available topic names", e);
        }

//        DescribeTopicsResult topicsResult = adminClient.describeTopics(List.of(sessionTopicPrefix + QF_SESSIONINFO_TOPIC_SUFFIX));
//        topicsResult.values().forEach((s, f) -> {
//            try {
//                TopicDescription topicDescription = f.get();
//                if (topicDescription != null) {
//                    log.info("Topic: {} => partitions: {}", topicDescription.name(), topicDescription.partitions());
//                } else {
//                    log.warn("Topic : {} does not exist", s);
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                log.error("Unable to get topic description for : {}", s, e);
                   // Topic does not exist - create new one
//            }
//
//        });

        return consumer;

    }

    private void createTopic(String topicName) throws InterruptedException {
        // Topic name "qf-session-FIX.4.4:QF_SRV->QF_CLIENT-1-info" is illegal, it contains a character other than ASCII alphanumerics, '.', '_' and '-'
        log.info("Creating topic: {}", topicName);
        NewTopic topic = new NewTopic(topicName, 1, (short) 1);
        try {
            adminClient.createTopics(List.of(topic)).all().get();
        } catch (ExecutionException e) {
            log.error("Unable to create topic: {}", topicName, e);
            throw new RuntimeException("Unable to create topic: " + topicName, e);
        }
    }

    @Override
    public boolean set(int sequence, String message) {
        return false;
    }

    @Override
    public void get(int startSequence, int endSequence, Collection<String> messages) {
        // todo: do not block on long range get (allow other methods to work - set, etc)
    }

    public boolean storeInMsg(int inSeq, String inMsg) {
        return false;
    }

    public void getInMsg(int startSeq, int endSeq, java.util.function.Consumer<String> inMsgConsumer) {
        // todo: implement
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
