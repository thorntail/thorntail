package org.wildfly.swarm.spi.api;

/**
 * @author Bob McWhirter
 */
public interface UserSpaceExtensionFactory {
    void configure() throws Exception;
}
