package org.wildfly.swarm.io;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class IORuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.io";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
