package org.wildfly.swarm.logging.format;

/**
 * @author Bob McWhirter
 */
public class PatternFormatter extends Formatter {

    private final String pattern;

    public PatternFormatter(String name, String pattern) {
        super(name, FormatterType.PATTERN);
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }
}
