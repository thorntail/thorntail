package org.wildfly.swarm.spi.api.config;

import java.util.List;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public interface ConfigTree {

    List asList();

    Map asMap();
}
