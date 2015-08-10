package org.wildfly.swarm.messaging;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class MessagingRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.messaging";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
