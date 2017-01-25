package org.wildfly.swarm.netflix.archaius.runtime;

/**
 * @author Bob McWhirter
 */
public class ArchaiusLinkage {

    private final String name;

    public ArchaiusLinkage(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

}
