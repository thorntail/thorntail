package org.wildfly.swarm.logstash;

import org.wildfly.swarm.container.RuntimeModuleProvider;

/**
 * @author Ken Finnigan
 */
public class LogstashRuntimeModuleProvider implements RuntimeModuleProvider {
    @Override
    public String getModuleName() {
        return "org.wildfly.swarm.logstash";
    }

    @Override
    public String getSlotName() {
        return "runtime";
    }
}
