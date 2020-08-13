package ru.home.mtur.quickfix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;
import java.io.InputStream;

import static quickfix.SessionSettings.*;

public class ConfigUtils {
    static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);

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

    public static SessionID getDefaultSessionID(SessionSettings settings) {
        Dictionary defaultSection = settings.get();
        try {
            return new SessionID(defaultSection.getString(BEGINSTRING),
                    defaultSection.getString(SENDERCOMPID), defaultSection.getString(TARGETCOMPID));
        } catch (ConfigError | FieldConvertError e) {
            throw new IllegalStateException("Unable to extract sessionID from default section of given settings", e);
        }
    }
}
