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

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

/**
 * A single option specification.
 *
 * <p>An option may have a short (single-dash) or long (double-dash) variant.</p>
 *
 * <p>Each option may optionally take a value, which may or may not be allowed
 * to be separated from the option.</p>
 *
 * <p>For instance, if {@link #valueMayBeSeparate(boolean)} is specified as false,
 * then only <code>-Dwhatever</code> would be allowed, while <code>-D whatever</code>
 * would not.</p>
 *
 * <p>Upon successful match, each <code>Option</code> has an {@link Action} associated
 * which is called with the value (if any) and the state-holding {@link CommandLine}
 * object.</p>
 *
 * @author Bob McWhirter
 */
@Vetoed
public class Option<T> {

    /**
     * Callback functional interface for matched options.
     */
    public interface Action<T> {
        /**
         * Perform some action, being passed the value and the <code>CommandLine</code>.
         *
         * @param commandLine The state-holding <code>CommandLine</code> object.
         * @param option      The matched option.
         * @param value       The value to the option, if any.  Possibly <code>null</code>.
         * @throws if an error occurs
         */
        void set(CommandLine commandLine, Option<T> option, String value) throws Exception;
    }

    /**
     * Construct an empty option.
     */
    public Option() {

    }

    /**
     * Associate an action with this option.
     *
     * @param action The action to call when matched.
     * @return This <code>Option</code> object.
     */
    public Option<T> then(Action<T> action) {
        this.action = action;
        return this;
    }

    /**
     * Display formatted help associated with this option.
     *
     * @param out The output stream to display the help upon.
     */
    public void displayHelp(PrintStream out) {
        out.println(merge(summary(), description()));
    }

    private List<String> summary() {
        List<String> list = new ArrayList<>();

        if (this.shortArg != null) {
            if (hasValue()) {
                if (this.valueDescription.contains("=")) {
                    list.add("-" + this.shortArg + this.valueDescription);
                } else {
                    list.add("-" + this.shortArg + "=" + this.valueDescription);
                }
                if (this.valueMayBeSeparate) {
                    if (this.valueDescription.contains("=")) {
                        list.add("-" + this.shortArg + this.valueDescription.replace('=', ' '));
                    } else {
                        list.add("-" + this.shortArg + " " + this.valueDescription);
                    }
                }
            } else {
                list.add("-" + this.shortArg);
            }
        }
        if (this.longArg != null) {
            if (hasValue()) {
                if (this.valueDescription.contains("=")) {
                    list.add("--" + this.longArg + this.valueDescription);
                } else {
                    list.add("--" + this.longArg + "=" + this.valueDescription);
                }
                if (this.valueMayBeSeparate) {
                    if (this.valueDescription.contains("=")) {
                        list.add("--" + this.longArg + this.valueDescription.replace('=', ' '));
                    } else {
                        list.add("--" + this.longArg + " " + this.valueDescription);
                    }
                }
            } else {
                list.add("--" + this.longArg);
            }
        }
        return list;
    }

    private static final int MAX_LINE_LENGTH = 50;

    private List<String> description() {
        List<String> list = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(this.description);


        StringBuilder line = new StringBuilder();

        while (tokens.hasMoreElements()) {
            String token = tokens.nextToken();

            if (line.length() + (" " + token).length() > MAX_LINE_LENGTH) {
                list.add(line.toString());
                line = new StringBuilder();
            }

            if (line.length() != 0) {
                line.append(" ");
            }
            line.append(token);
        }

        list.add(line.toString());

        return list;
    }

    private String merge(List<String> summary, List<String> desc) {
        if (summary.size() > desc.size()) {
            int diff = summary.size() - desc.size();

            for (int i = 0; i < diff; ++i) {
                desc.add("");
            }
        }

        if (desc.size() > summary.size()) {
            int diff = desc.size() - summary.size();

            for (int i = 0; i < diff; ++i) {
                summary.add("");
            }
        }

        StringBuilder txt = new StringBuilder();

        for (int i = 0; i < summary.size(); ++i) {
            txt.append(String.format("  %-30s     %s", summary.get(i), desc.get(i)));
            txt.append("\n");
        }

        return txt.toString();
    }


    /**
     * Specify a short (single-dash) variant.
     *
     * <p>A single character following a single dash, such as <code>-c</code>.</p>
     *
     * @param shortArg The character.
     * @return This <code>Option</code> object.
     */
    public Option<T> withShort(Character shortArg) {
        this.shortArg = shortArg;
        return this;
    }

    /**
     * Specify a long (double-dash) variant.
     *
     * <p>A string following a double-dash, such as <code>--help</code>.</p>
     *
     * @param longArg The string (without dashes).
     * @return This <code>Option</code> object.
     */
    public Option<T> withLong(String longArg) {
        this.longArg = longArg;
        return this;
    }


    /**
     * Set a human-readable description of this option.
     *
     * @param description The description.
     * @return This <code>Option</code> object.
     */
    public Option<T> withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Specify that this option takes an argument value.
     *
     * <p>A non-null string passed to this method will be used to signify
     * that this option may take an argument, and to describe its format.</p>
     *
     * <p>For instance a string of <code>name[=value]</code></p> would indicate
     * that a <code>name</code> is expected, with an optional <code>value</code>
     * which should follow an equal sign.</p>
     *
     * @param valueDescription
     * @return
     */
    public Option<T> hasValue(String valueDescription) {
        this.valueDescription = valueDescription;
        return this;
    }

    private boolean hasValue() {
        return this.valueDescription != null;
    }

    /**
     * Indicate if the value to the option may be separated by whitespace. (defaults to <code>true</code>).
     *
     * <p>In some cases, because of ambiguity, it may be required to indicate that an
     * argument may <b>not</b> be separate from the option.  If an argument is to be
     * provided, it must immediately follow the option without whitespace.</p>
     *
     * @param valueMayBeSeparate <code>true</code> (the default) if value may be separate, or <code>false</code> to indicate it must be conjoined.
     * @return This <code>Option</code> object.
     */
    public Option<T> valueMayBeSeparate(boolean valueMayBeSeparate) {
        this.valueMayBeSeparate = valueMayBeSeparate;
        return this;
    }

    /**
     * Specify a default value supplier for this option.
     *
     * @param supplier The supplier.
     * @return This <code>Option</code> object.
     */
    public Option<T> withDefault(Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    T defaultValue() {
        if (this.supplier == null) {
            return null;
        }
        return this.supplier.get();
    }

    boolean parse(ParseState state, CommandLine commandLine) throws Exception {
        String cur = state.la();

        if (cur == null) {
            return false;
        }

        String value = null;

        String matchedArg = null;

        if (this.shortArg != null && cur.startsWith("-" + this.shortArg)) {
            matchedArg = "-" + this.shortArg;
            if (hasValue() && cur.length() >= 3) {
                if (cur.charAt(2) == '=') {
                    value = cur.substring(3);
                } else {
                    value = cur.substring(2);
                }
            }
        } else if (this.longArg != null && cur.startsWith("--" + this.longArg)) {
            matchedArg = "--" + this.longArg;
            if (hasValue() && cur.length() >= this.longArg.length() + 3) {
                if (cur.charAt(this.longArg.length() + 3) == '=') {
                    value = cur.substring(this.longArg.length() + 3);
                } else {
                    value = cur.substring(this.longArg.length() + 2);
                }
            }
        } else {
            return false;
        }

        state.consume();

        if (value != null && value.trim().isEmpty()) {
            value = null;
        }

        if (hasValue() && value == null && !this.valueMayBeSeparate) {
            throw new RuntimeException(matchedArg + " requires an argument");
        }

        if (hasValue() && value == null) {
            if (state.la() == null) {
                throw new RuntimeException(matchedArg + " requires an argument");
            }
            value = state.consume();
        }

        this.action.set(commandLine, this, value);

        return true;
    }

    public String toString() {
        return "[Option: short=" + this.shortArg + "; long=" + this.longArg + "; valueDescription=" + this.valueDescription + "]";
    }

    /**
     * Helper to attempt forming a URL from a String in a sensible fashion.
     *
     * @param value The input string.
     * @return The correct URL, if possible.
     */
    public static URL toURL(String value) throws MalformedURLException {
        try {
            URL url = new URL(value);
            return url;
        } catch (MalformedURLException e) {
            try {
                return new File(value).toURI().toURL();
            } catch (MalformedURLException e2) {
                // throw the original
                throw e;
            }
        }
    }

    private Character shortArg;

    private String longArg;

    private String valueDescription;

    private boolean valueMayBeSeparate = true;

    private Action<T> action;

    private String description;

    private Supplier<T> supplier;


}
