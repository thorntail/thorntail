package org.wildfly.swarm.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

/**
 * Created by hbraun on 22.08.17.
 */
public class MavenArgsParser {

    public enum ARG {
            F("-f", "--file"),
            P("-P", "--activate-profiles");

        // the flag is the short form
        private final String flag;

        // the alt flag is the long form (different syntax applies)
        private final String altFlag;

        ARG(String flag, String altFlag) {
            this.flag = flag;
            this.altFlag = altFlag;
        }

        public String getFlag() {
            return flag;
        }
        public String getAltFlag() {
            return altFlag;
        }
    }

    private Map<ARG, String> argValues = new HashMap<>();

    private MavenArgsParser(String commandLine) {

        StringTokenizer tok = new StringTokenizer(commandLine, " ");
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            ARG[] args = ARG.values();
            for (ARG arg : args) {

                // irregular values, without whitespace between flag and value
                Optional<String> irregularValue = parseIrregularSyntax(arg, token);
                if (irregularValue.isPresent()) {
                    argValues.put(arg, irregularValue.get());
                    continue;
                }

                // regular syntax with whitespace between flag and value
                if (token.equals(arg.flag) && tok.hasMoreTokens()) {
                    argValues.put(arg, tok.nextToken());
                } else if (token.equals(arg.altFlag) && tok.hasMoreTokens()) {
                    argValues.put(arg, tok.nextToken());
                }
            }
        }
    }

    private Optional<String> parseIrregularSyntax(ARG arg, String token) {
        return extract(arg.flag, token); // irregular syntax only applies to the short form of flags
    }

    private static Optional<String> extract(String flag, String token) {

        Optional<String> result = Optional.empty();

        if (token.startsWith(flag) && token.length() > flag.length()) {
            result = Optional.of(token.substring(token.indexOf(flag) + flag.length()));
        }

        return result;
    }

    public Optional<String> get(ARG arg) {
        return argValues.containsKey(arg) ? Optional.of(argValues.get(arg)) : Optional.empty();
    }

    public static MavenArgsParser parse(String commandLine) {
        if (null == commandLine) {
            throw new IllegalArgumentException("commandLine cannot be null");
        }

        return new MavenArgsParser(commandLine);
    }
}
