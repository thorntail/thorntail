package org.wildfly.swarm.logging;

/**
 * @author Bob McWhirter
 */
public class Formatter {

    private final String name;
    private final String pattern;

    public Formatter(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public String getName() {
        return this.name;
    }

    public String getPattern() {
        return this.pattern;
    }
}
