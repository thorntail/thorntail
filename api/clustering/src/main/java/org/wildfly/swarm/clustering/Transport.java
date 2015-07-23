package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class Transport {

    private final String name;
    private final String socketBinding;

    public Transport(String name, String socketBinding) {
        this.name = name;
        this.socketBinding = socketBinding;
    }

    public String name() {
        return this.name;
    }

    public String socketBinding() {
        return this.socketBinding;
    }
}
