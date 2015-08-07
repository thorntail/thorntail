package org.wildfly.swarm.container;

import java.io.IOException;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Bob McWhirter
 */
public interface Deployer {
    void deploy(Archive deployment) throws IOException;
}
