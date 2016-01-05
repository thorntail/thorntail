package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class BaseSocketBinding {

    private final String name;

    protected BaseSocketBinding(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }
}
