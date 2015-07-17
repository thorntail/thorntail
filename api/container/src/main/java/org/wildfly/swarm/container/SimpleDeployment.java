package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public class SimpleDeployment implements Deployment {

    private final Archive archive;

    public SimpleDeployment(Archive archive) {
        this.archive = archive;
    }

    @Override
    public Archive getArchive(boolean finalize) {
        return this.archive;
    }

    @Override
    public Archive getArchive() {
        return getArchive(false);
    }
}
