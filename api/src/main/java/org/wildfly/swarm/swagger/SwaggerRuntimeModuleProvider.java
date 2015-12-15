package org.wildfly.swarm.swagger;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Lance Ball
 */
public class SwaggerRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.swagger";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
