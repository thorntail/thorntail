package org.wildfly.swarm.arquillian.adapter.resources;

/**
 * @author Bob McWhirter
 */
public class ContextRoot {

    private final String context;

    public ContextRoot(String context) {
        this.context = context;
    }

    public String context() {
        return this.context;
    }
}
