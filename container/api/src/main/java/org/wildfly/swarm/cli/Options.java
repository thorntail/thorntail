package org.wildfly.swarm.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class Options {

    private List<Option> options = new ArrayList<>();

    public Options() {

    }

    public Options(Option... options) {
        for (Option option : options) {
            withOption(option);
        }
    }

    public Options withOption(Option option) {
        this.options.add(option);
        return this;
    }

    public void displayHelp(PrintStream out) {

        for (Option option : this.options) {
            option.displayHelp(out);
        }
    }

    public void parse(ParseState state, CommandLine commandLine) {
        OUTER:
        while (state.la() != null) {
            INNER:
            for (Option option : this.options) {
                if (option.parse(state, commandLine)) {
                    continue OUTER;
                }
            }
            String arg = state.consume();
            if ( arg.startsWith( "-" ) ) {
                commandLine.invalidArgument( arg );
            }
            commandLine.extraArgument( arg );
        }
    }

}
