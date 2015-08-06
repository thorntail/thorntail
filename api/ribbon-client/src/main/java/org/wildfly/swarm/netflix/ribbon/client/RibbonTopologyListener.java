package org.wildfly.swarm.netflix.ribbon.client;

/**
 * @author Bob McWhirter
 */
public interface RibbonTopologyListener {
    void onChange(RibbonTopology topology);
}
