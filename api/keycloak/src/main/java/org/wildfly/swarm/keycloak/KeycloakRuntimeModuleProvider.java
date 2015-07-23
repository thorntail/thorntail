package org.wildfly.swarm.keycloak;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class KeycloakRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.keycloak";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
