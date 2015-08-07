package org.wildfly.swarm.naming;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class NamingRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.naming";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
