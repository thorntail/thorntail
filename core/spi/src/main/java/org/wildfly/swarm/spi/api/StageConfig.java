package org.wildfly.swarm.spi.api;

import org.wildfly.swarm.spi.api.config.Resolver;

/**
 * @author Bob McWhirter
 */
@Deprecated
public interface StageConfig {
    @Deprecated
    default Resolver<String> resolve(String name) {
        return null;
    }
}
