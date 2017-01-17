package org.wildfly.swarm.spi.api.config;

import java.util.Properties;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public interface ConfigView {

    Object valueOf(ConfigKey key);

    Resolver<String> resolve(ConfigKey key);

    default Resolver<String> resolve(String name) {
        return resolve(ConfigKey.parse(name));
    }

    Set<SimpleKey> simpleSubkeys(ConfigKey prefix);

    boolean hasKeyOrSubkeys(ConfigKey subPrefix);

    Properties asProperties();
}
