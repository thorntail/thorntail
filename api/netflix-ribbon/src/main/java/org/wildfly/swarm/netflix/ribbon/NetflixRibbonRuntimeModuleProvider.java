package org.wildfly.swarm.netflix.ribbon;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class NetflixRibbonRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.netflix.ribbon";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
