package org.wildfly.swarm.remoting;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class RemotingRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.remoting";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
