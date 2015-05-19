package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface Deployer {
    void deploy(Archive deployment) throws IOException;
}
