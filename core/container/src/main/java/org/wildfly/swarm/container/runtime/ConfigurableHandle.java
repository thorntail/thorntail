package org.wildfly.swarm.container.runtime;

import org.wildfly.swarm.spi.api.config.ConfigKey;

/**
 * @author Bob McWhirter
 */
public interface ConfigurableHandle {
    ConfigKey key();

    Class<?> type() throws Exception;

    <T> void set(T value) throws Exception;

    <T> T currentValue() throws Exception;
}
