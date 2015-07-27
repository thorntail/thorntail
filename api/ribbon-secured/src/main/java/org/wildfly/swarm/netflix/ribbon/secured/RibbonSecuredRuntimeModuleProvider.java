package org.wildfly.swarm.netflix.ribbon.secured;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class RibbonSecuredRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.netflix.ribbon.secured";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
