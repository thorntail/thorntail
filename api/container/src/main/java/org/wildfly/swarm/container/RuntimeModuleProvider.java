package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface RuntimeModuleProvider {

    String getModuleName();

    default String getSlotName() {
        return "main";
    }
}
