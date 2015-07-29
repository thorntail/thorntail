package org.wildfly.swarm.logging.handlers;

/**
 * @author Bob McWhirter
 */
public class ConsoleHandler {

    private final String level;

    private final String formatter;

    public ConsoleHandler(String level, String formatter) {
        this.level = level;
        this.formatter = formatter;
    }

    public String getLevel() {
        return this.level;
    }

    public String getFormatter() {
        return this.formatter;
    }
}
