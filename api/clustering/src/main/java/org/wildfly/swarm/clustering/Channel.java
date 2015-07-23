package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class Channel {

    private final String name;
    private String stack;

    public Channel(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Channel stack(String stack) {
        this.stack = stack;
        return this;
    }

    public Channel stack(Stack stack) {
        this.stack = stack.name();
        return this;
    }

    public String stack() {
        return this.stack;
    }
}
