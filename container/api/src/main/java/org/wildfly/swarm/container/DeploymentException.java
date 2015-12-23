package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public class DeploymentException extends Exception {

    private final Archive<?> archive;

    public DeploymentException(String message) {
        this.archive = null;
    }

    public DeploymentException(Throwable rootCause) {
        super( rootCause );
        this.archive = null;
    }

    public DeploymentException(Archive<?> archive, Throwable rootCause) {
        super( rootCause );
        this.archive = archive;
    }

    public DeploymentException(Archive<?> archive, String message) {
        super( message );
        this.archive = archive;
    }

    public Archive<?> getArchive() {
        return this.archive;
    }

}
