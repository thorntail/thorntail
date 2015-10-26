package org.wildfly.swarm.ribbon.webapp;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Lance Ball
 */
public class RibbonWebAppRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.ribbon.webapp";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
