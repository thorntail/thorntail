package org.wildfly.swarm.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

/**
 * @author Bob McWhirter
 */
public class Option {

    private Character shortArg;

    private String longArg;

    private String valueDescription;

    private boolean valueMayBeSeparate = true;

    private Action action;

    private String description;

    public static interface Action {
        void set(CommandLine commandLine, String value);
    }

    public Option(Action action) {
        this.action = action;
    }

    public Option() {

    }

    public Option then(Action action) {
        this.action = action;
        return this;
    }

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


    public Option withShort(Character shortArg) {
        this.shortArg = shortArg;
        return this;
    }

    public Option withDescription(String description) {
        this.description = description;
        return this;
    }

    public Option withLong(String longArg) {
        this.longArg = longArg;
        return this;
    }

    public Option hasValue(String valueDescription) {
        this.valueDescription = valueDescription;
        return this;
    }

    private boolean hasValue() {
        return this.valueDescription != null;
    }

    public Option valueMayBeSeparate(boolean valueMayBeSeparate) {
        this.valueMayBeSeparate = valueMayBeSeparate;
        return this;
    }

    public boolean parse(ParseState state, CommandLine commandLine) {
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

        this.action.set(commandLine, value);

        return true;
    }

    public String toString() {
        return "[Option: short=" + this.shortArg + "; long=" + this.longArg + "; valueDescription=" + this.valueDescription + "]";

    }

}
