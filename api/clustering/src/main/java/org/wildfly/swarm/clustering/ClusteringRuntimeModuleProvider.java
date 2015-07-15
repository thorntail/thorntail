package org.wildfly.swarm.clustering;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class ClusteringRuntimeModuleProvider implements RuntimeModuleProvider {
    public String getModuleName() {
        return "org.wildfly.swarm.clustering";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
