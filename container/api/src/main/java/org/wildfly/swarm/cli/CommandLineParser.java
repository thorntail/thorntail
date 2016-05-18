package org.wildfly.swarm.cli;

/**
 * @author Bob McWhirter
 */
class CommandLineParser {

    private final Options options;

    private CommandLineParser(Options options) {
        this.options = options;
    }

    private CommandLine parseInternal(String... args) throws Exception {
        ParseState state = new ParseState(args);
        CommandLine commandLine = new CommandLine(this.options);

        while (state.la() != null) {
            this.options.parse(state, commandLine);
        }
        return commandLine;
    }

    static CommandLine parse(Options options, String... args) throws Exception {
        CommandLineParser parser = new CommandLineParser(options);
        return parser.parseInternal(args);
    }
}
