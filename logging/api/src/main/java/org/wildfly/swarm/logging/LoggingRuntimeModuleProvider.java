package org.wildfly.swarm.logging;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class LoggingRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.logging";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
