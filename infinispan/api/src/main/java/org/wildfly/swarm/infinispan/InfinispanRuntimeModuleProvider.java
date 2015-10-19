package org.wildfly.swarm.infinispan;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Lance Ball
 */
public class InfinispanRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.infinispan";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
