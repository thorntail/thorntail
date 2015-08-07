package org.wildfly.swarm.jpa;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class JPARuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.jpa";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
