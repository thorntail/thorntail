package org.wildfly.swarm.logging.handlers;

/**
 * @author Ken Finnigan
 */
public class Handler {

    private final String name;

    private final String level;

    private final String formatter;

    private final HandlerType type;

    public Handler(String name, String level, String formatter) {
        this(name, level, formatter, HandlerType.FILE);
    }

    public Handler(String name, String level, String formatter, HandlerType type) {
        this.name = name;
        this.level = level;
        this.formatter = formatter;
        this.type = type;
    }

    public String name() {
        return this.name;
    }

    public String level() {
        return this.level;
    }

    public String formatter() {
        return this.formatter;
    }

    public boolean isFile() {
        return this.type == HandlerType.FILE;
    }

    public boolean isCustom() {
        return this.type == HandlerType.CUSTOM;
    }

    enum HandlerType {
        FILE,
        CUSTOM
    }
}
