package org.wildfly.swarm.spi.api;

import java.util.Map;

/**
 * @author Heiko Braun
 * @since 08/04/16
 */
public interface ProjectStage {

    String getName();

    Map<String,String> getProperties();
}

