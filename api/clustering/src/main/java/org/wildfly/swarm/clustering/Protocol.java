package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class Protocol {

    private final String name;

    public Protocol(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }
}
