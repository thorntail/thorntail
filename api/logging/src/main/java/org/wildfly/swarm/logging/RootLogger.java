package org.wildfly.swarm.logging;

/**
 * @author Bob McWhirter
 */
public class RootLogger {
    private final String handler;

    private final String level;

    public RootLogger(String handler, String level) {
        this.handler = handler;
        this.level = level;
    }

    public String getHandler() {
        return this.handler;
    }

    public String getLevel() {
        return this.level;
    }
}
