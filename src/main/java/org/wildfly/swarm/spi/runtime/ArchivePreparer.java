package org.wildfly.swarm.spi.runtime;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface ArchivePreparer {
    void prepareArchive(Archive<?> archive);
}
