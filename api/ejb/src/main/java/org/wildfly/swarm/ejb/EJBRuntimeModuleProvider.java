package org.wildfly.swarm.ejb;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class EJBRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.ejb";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
