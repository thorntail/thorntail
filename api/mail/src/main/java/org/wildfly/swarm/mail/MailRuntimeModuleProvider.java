package org.wildfly.swarm.mail;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class MailRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.mail";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
