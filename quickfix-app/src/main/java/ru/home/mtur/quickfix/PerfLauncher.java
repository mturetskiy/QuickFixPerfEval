package ru.home.mtur.quickfix;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import ru.home.mtur.quickfix.client.OneSessionQuickFixClient;
import ru.home.mtur.quickfix.server.QuickFixServer;

import static ru.home.mtur.quickfix.utils.ConfigUtils.CLI_OPTIONS;
import static ru.home.mtur.quickfix.utils.ConfigUtils.CLI_PARSER;

public class PerfLauncher {
    static final Logger log = LoggerFactory.getLogger(PerfLauncher.class);

    public static void main(String[] args) throws FieldConvertError, ConfigError, InterruptedException {
        try {
            CommandLine cmd = CLI_PARSER.parse(CLI_OPTIONS, args);
            if (cmd.hasOption("client")) {
                OneSessionQuickFixClient.main(args);
            } else if (cmd.hasOption("server")) {
                QuickFixServer.main(args);
            }
        } catch (ParseException e) {
            new HelpFormatter().printHelp( "PerfLauncher", CLI_OPTIONS);
            throw new IllegalArgumentException("Unable to parse command line args", e);
        }
    }
}
