package org.wildfly.swarm.topology;

/**
 * @author Bob McWhirter
 */
public interface TopologyListener {
    void onChange(Topology topology);
}
