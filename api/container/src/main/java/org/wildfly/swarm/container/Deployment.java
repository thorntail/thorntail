package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface Deployment {
    Archive getArchive();
}
