package org.wildfly.swarm.connector;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class ConnectorRuntimeModuleProvider implements RuntimeModuleProvider {
    public String getModuleName() {
        return "org.wildfly.swarm.connector";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
