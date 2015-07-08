package org.wildfly.swarm.jsf;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class JSFRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.jsf";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
