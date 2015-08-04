package org.wildfly.swarm.netflix.ribbon;

/**
 * @author Bob McWhirter
 */
public interface RibbonTopologyListener {
    void onChange(RibbonTopology topology);
}
