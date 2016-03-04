package org.wildfly.swarm.spi.api;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface ArtifactLookup {

    Archive artifact(String gav) throws Exception;
    Archive artifact(String gav, String asName) throws Exception;
}
