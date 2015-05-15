package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public interface Server {

    Deployer start(Container config) throws Exception;
}
