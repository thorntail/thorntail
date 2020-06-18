/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.spi.api.config;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.wildfly.swarm.spi.api.StageConfig;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("deprecation")
public interface ConfigView extends StageConfig {

    Object valueOf(ConfigKey key);

    Resolver<?> resolverFor(ConfigKey key);

    default Resolver<String> resolve(ConfigKey key) {
        return resolverFor(key).as(String.class);
    }

    default Resolver<String> resolve(String name) {
        return resolve(ConfigKey.parse(name));
    }

    Stream<ConfigKey> allKeysRecursively();

    List<SimpleKey> simpleSubkeys(ConfigKey prefix);

    boolean hasKeyOrSubkeys(ConfigKey subPrefix);

    Properties asProperties();
}
