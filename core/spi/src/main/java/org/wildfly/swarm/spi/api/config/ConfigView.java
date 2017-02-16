package org.wildfly.swarm.spi.api.config;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Bob McWhirter
 */
public interface ConfigView extends StageConfig {

    Object valueOf(ConfigKey key);

    Resolver<String> resolve(ConfigKey key);

    default Resolver<String> resolve(String name) {
        return resolve(ConfigKey.parse(name));
    }

    Stream<ConfigKey> allKeysRecursively();

    Set<SimpleKey> simpleSubkeys(ConfigKey prefix);

    boolean hasKeyOrSubkeys(ConfigKey subPrefix);

    Properties asProperties();
}
