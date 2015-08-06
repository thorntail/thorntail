package org.wildfly.swarm.netflix.ribbon.client;

import java.util.List;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public interface RibbonTopology {
    void addListener(RibbonTopologyListener listener);
    void removeListener(RibbonTopologyListener listener);
    Map<String,List<String>> asMap();
}
