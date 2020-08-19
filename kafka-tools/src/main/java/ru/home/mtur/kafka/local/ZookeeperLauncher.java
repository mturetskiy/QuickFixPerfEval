package ru.home.mtur.kafka.local;

import org.apache.zookeeper.jmx.ManagedUtil;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.admin.AdminServer.AdminServerException;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import java.io.IOException;

import static ru.home.mtur.quickfix.utils.ConfigUtils.getAppConfig;
import static ru.home.mtur.quickfix.utils.ConfigUtils.getClasspathResourcePath;

public class ZookeeperLauncher {
    static final Logger log = LoggerFactory.getLogger(ZookeeperLauncher.class);

    public static final String DEFAULT_CONFIG_NAME = "zk-conf.properties";

    private ZooKeeperServerMain zkServer;
    private ServerConfig zkServerConfig;

    public ZookeeperLauncher(String confFileName) {
        String path = getClasspathResourcePath(confFileName);
        zkServerConfig = new ServerConfig();
        try {
            zkServerConfig.parse(path);
        } catch (QuorumPeerConfig.ConfigException e) {
            throw new IllegalArgumentException("Unable to load ZK pros", e);
        }

        zkServer = new ZooKeeperServerMain();

        log.info("ZK configs has been loaded successfully.");
    }

    public static void main(String[] args) {
        String appConfigName = getAppConfig(args, DEFAULT_CONFIG_NAME);
        ZookeeperLauncher launcher = new ZookeeperLauncher(appConfigName);
        launcher.start();


    }

    public void start() {
        if (zkServer == null) {
            throw new RuntimeException("ZK server is not configured yet.");
        }

        new Thread(() -> {
            try {
                log.info("Starting ZK server ...");
                ManagedUtil.registerLog4jMBeans();
                zkServer.runFromConfig(zkServerConfig);
            } catch (IOException | AdminServerException | JMException e) {
                log.error("ZooKeeper start failed", e);
            }
        }).start();
    }
}
