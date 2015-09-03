package org.wildfly.swarm.jolokia;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class JolokiaRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.jolokia";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
