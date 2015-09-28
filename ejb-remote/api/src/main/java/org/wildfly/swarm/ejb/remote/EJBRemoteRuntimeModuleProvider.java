package org.wildfly.swarm.ejb.remote;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.ejb.remote";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
