package org.wildfly.swarm.spi.api;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface ArchivePreparer {
    void prepareArchive(Archive<?> archive);
}
