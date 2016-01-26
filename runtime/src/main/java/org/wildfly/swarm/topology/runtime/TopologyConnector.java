package org.wildfly.swarm.topology.runtime;

import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public interface TopologyConnector {
    ServiceName SERVICE_NAME = ServiceName.of("swarm", "topology", "connector");

    void advertise(String name);

    void unadvertise(String name);
}
