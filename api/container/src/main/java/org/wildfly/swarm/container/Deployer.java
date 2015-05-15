package org.wildfly.swarm.container;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public interface Deployer {

    void deploy(Deployment deployment) throws IOException;
}
