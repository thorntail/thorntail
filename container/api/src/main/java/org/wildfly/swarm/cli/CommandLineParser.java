package org.wildfly.swarm.cli;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class CommandLineParser {

    private final Options options;

    private CommandLineParser(String... args) {

        this.options = new Options(
                new Option()
                        .withLong("help")
                        .withShort('h')
                        .withDescription("Display this help")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.HELP, true);
                        }),

                new Option()
                        .withLong("version")
                        .withShort('v')
                        .withDescription("Display the version of WildFly Swarm")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.VERSION, true);
                        }),

                new Option()
                        .withShort('D')
                        .hasValue("<name>[=<value>]")
                        .valueMayBeSeparate(false)
                        .withDescription("Set a system property")
                        .then((cmd, value) -> {
                            String[] nameValue = value.split("=");
                            Properties props = cmd.get(CommandLine.PROPERTIES);
                            String propName = nameValue[0];
                            String propValue = "true";
                            if (nameValue.length > 1) {
                                propValue = nameValue[1];
                            }
                            props.setProperty(propName, propValue);
                        }),

                new Option()
                        .withShort('P')
                        .withLong("properties")
                        .hasValue("<url>")
                        .withDescription("Load system properties from the given URL")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.PROPERTIES_URL, value);
                        }),

                new Option()
                        .withShort('c')
                        .withLong("server-config")
                        .hasValue("<config>")
                        .valueMayBeSeparate(true)
                        .withDescription("URL of the server configuration (e.g. standalone.xml)")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.CONFIG, value);
                        }),

                new Option()
                        .withShort('s')
                        .withLong("stage-config")
                        .hasValue("<config>")
                        .valueMayBeSeparate(true)
                        .withDescription("URL to the stage configuration (e.g. config.yaml")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.STAGE_CONFIG, value);
                        }),

                new Option()
                        .withShort('b')
                        .hasValue("<value>")
                        .valueMayBeSeparate(true)
                        .withDescription("Set the property swarm.bind.address to <value>")
                        .then((cmd, value) -> {
                            cmd.put(CommandLine.BIND, value);
                        })
        );
    }

    private CommandLine parseInternal(String... args) {
        ParseState state = new ParseState(args);
        CommandLine commandLine = new CommandLine(this.options);

        while (state.la() != null) {
            this.options.parse(state, commandLine);
        }
        return commandLine;
    }

    public static CommandLine parse(String... args) {
        CommandLineParser parser = new CommandLineParser();
        return parser.parseInternal(args);
    }
}
