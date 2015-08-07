package org.wildfly.swarm.datasources;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class DatasourcesRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.datasources";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
