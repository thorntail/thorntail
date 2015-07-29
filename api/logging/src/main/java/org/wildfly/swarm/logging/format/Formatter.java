package org.wildfly.swarm.logging.format;

/**
 * @author Ken Finnigan
 */
public class Formatter {

    private final String name;

    private final FormatterType type;

    public Formatter(String name) {
        this(name, FormatterType.PATTERN);
    }

    public Formatter(String name, FormatterType formatterType) {
        this.name = name;
        this.type = formatterType;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPattern() {
        return this.type == FormatterType.PATTERN;
    }

    public boolean isCustom() {
        return this.type == FormatterType.CUSTOM;
    }

    enum FormatterType {
        PATTERN,
        CUSTOM
    }
}
