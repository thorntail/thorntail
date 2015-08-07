package org.wildfly.swarm.weld;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class WeldRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.weld";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
