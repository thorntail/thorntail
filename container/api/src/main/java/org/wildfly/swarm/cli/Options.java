package org.wildfly.swarm.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/** Options for command-line parsing.
 *
 * Options are matched in the order in which they are added.
 *
 * @author Bob McWhirter
 */
public class Options {


    /** Construct.
     *
     * @param options Zero or more options use initially.
     */
    public Options(Option... options) {
        for (Option option : options) {
            this.options.add(option);
        }
    }

    /** Fluent method to add more options.
     *
     * @param option The option to add.
     * @return This options object.
     */
    public Options withOption(Option option) {
        this.options.add(option);
        return this;
    }

    /** Display the help to the specified output stream.
     *
     * @param out The output stream.
     */
    public void displayHelp(PrintStream out) {
        for (Option option : this.options) {
            option.displayHelp(out);
        }
    }

    void parse(ParseState state, CommandLine commandLine) throws Exception {
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

    private final List<Option> options = new ArrayList<>();


}
