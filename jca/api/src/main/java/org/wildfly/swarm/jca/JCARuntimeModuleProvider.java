package org.wildfly.swarm.jca;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class JCARuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.jca";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
