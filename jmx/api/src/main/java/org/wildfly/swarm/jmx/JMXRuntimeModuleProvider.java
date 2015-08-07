package org.wildfly.swarm.jmx;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class JMXRuntimeModuleProvider implements RuntimeModuleProvider {
    public String getModuleName() {
        return "org.wildfly.swarm.jmx";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
