package org.wildfly.swarm.spi.api;

/**
 * Created by bob on 5/17/17.
 */
public interface DeploymentProcessor {
    void process() throws Exception;
}
