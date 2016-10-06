/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Vetoed;

/** Options for command-line parsing.
 *
 * Options are matched in the order in which they are added.
 *
 * @author Bob McWhirter
 */
@Vetoed
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
