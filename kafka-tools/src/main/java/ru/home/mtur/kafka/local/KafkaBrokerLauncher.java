package ru.home.mtur.kafka.local;

import kafka.metrics.KafkaMetricsReporter;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.apache.kafka.common.utils.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static ru.home.mtur.quickfix.utils.ConfigUtils.loadProperties;

public class KafkaBrokerLauncher {
    static final Logger log = LoggerFactory.getLogger(KafkaBrokerLauncher.class);

    public static final String DEFAULT_CONFIG_NAME = "/kafka-broker-conf.properties";
    private KafkaServer server;

    public KafkaBrokerLauncher(String confFileName) {
        Properties properties = loadProperties(confFileName);
        cleanupPrevLogs(properties);
        KafkaConfig kafkaConfig = KafkaConfig.fromProps(properties);

        List<KafkaMetricsReporter> reportersList = new ArrayList<>();
        Seq<KafkaMetricsReporter> reportersSeq = JavaConverters.asScalaBufferConverter(reportersList).asScala();
        server = new KafkaServer(kafkaConfig, new SystemTime(), Option.apply("Kaffka"), reportersSeq);

        log.info("Kafka broker has been configured");
    }

    private void cleanupPrevLogs(Properties properties) {
        String logsDir = properties.getProperty("log.dirs");
        File file = new File(logsDir);

        if (file.exists() && file.isDirectory()) {
            log.info("Cleaning kafka log dir: {}", file.getAbsoluteFile());

            try {
                Files.walk(file.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException("Unable to cleanup prev. kafka broker logs", e);
            }
        }
    }

    public static void main(String[] args) {
        KafkaBrokerLauncher launcher = new KafkaBrokerLauncher(DEFAULT_CONFIG_NAME);
        Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
        launcher.start();
    }

    public void start() {
        server.startup();
    }

    public void stop() {
        server.shutdown();
    }
}
