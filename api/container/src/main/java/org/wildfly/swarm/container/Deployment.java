package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;

/** A ShrinkWrap-based deployment.
 *
 * @author Bob McWhirter
 */
public interface Deployment {
    Archive getArchive(boolean finalize);
    Archive getArchive();
}
