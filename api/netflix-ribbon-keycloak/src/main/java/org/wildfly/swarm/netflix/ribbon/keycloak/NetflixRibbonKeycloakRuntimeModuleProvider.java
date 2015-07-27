package org.wildfly.swarm.netflix.ribbon.keycloak;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Bob McWhirter
 */
public class NetflixRibbonKeycloakRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.netflix.ribbon.keycloak";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
