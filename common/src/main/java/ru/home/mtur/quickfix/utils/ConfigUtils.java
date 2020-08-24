package ru.home.mtur.quickfix.utils;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static quickfix.SessionSettings.*;

public class ConfigUtils {
    static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    public static CommandLineParser CLI_PARSER = new DefaultParser();
    public static Options CLI_OPTIONS = new Options()
            .addOption("client", "Run client app")
            .addOption("server", "Run server app")
            .addOption("p", true,"Config file name")
            .addOption("h", true,"Server host (where to connect)");

    public static SessionSettings loadSessionSettings(String fileName) {
        SessionSettings settings = null;
        try (InputStream configFileResource = ConfigUtils.class.getResourceAsStream(fileName)) {
            settings = new SessionSettings(configFileResource);
        } catch (ConfigError e) {
            log.error("Exception during session settings loading from file: {}", fileName, e);
            throw new IllegalStateException("Unable to load session settings", e);
        } catch (IOException e) {
            log.error("Unable to close input stream", e);
        }

        return settings;
    }

    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream propsFileResource = ConfigUtils.class.getResourceAsStream(fileName)) {
            props.load(propsFileResource);
        } catch (IOException e) {
            log.error("Unable to close input stream", e);
        }

        return props;
    }

    public static String getClasspathResourcePath(String name) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
        return resource!= null ? resource.getFile() : null;
    }

    public static SessionID getDefaultSessionID(SessionSettings settings) {
        Dictionary defaultSection = settings.get();
        try {
            return new SessionID(defaultSection.getString(BEGINSTRING),
                    defaultSection.getString(SENDERCOMPID), defaultSection.getString(TARGETCOMPID));
        } catch (ConfigError | FieldConvertError e) {
            throw new IllegalStateException("Unable to extract sessionID from default section of given settings", e);
        }
    }

    public static String getAppConfig(String[] args, String defaultConfig) {
        try {
            CommandLine cmd = CLI_PARSER.parse(CLI_OPTIONS, args);
            return cmd.getOptionValue("p", defaultConfig);
        } catch (ParseException e) {
            new HelpFormatter().printHelp( "<CLASS>", CLI_OPTIONS );
            throw new IllegalArgumentException("Unable to parse command line args", e);
        }
    }

    public static String getServerHost(String[] args, String defaultHost) {
        try {
            CommandLine cmd = CLI_PARSER.parse(CLI_OPTIONS, args);
            return cmd.getOptionValue("h", defaultHost);
        } catch (ParseException e) {
            new HelpFormatter().printHelp( "<CLASS>", CLI_OPTIONS );
            throw new IllegalArgumentException("Unable to parse command line args", e);
        }

    }
}
