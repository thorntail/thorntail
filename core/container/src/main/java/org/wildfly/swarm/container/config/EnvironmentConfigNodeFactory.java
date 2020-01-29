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
package org.wildfly.swarm.container.config;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.config.ConfigKey;

/**
 * Factory capable ofr building a {@code ConfigNode} tree from a {@link Properties} object.
 *
 * @author Bob McWhirter
 */
@Vetoed
public class EnvironmentConfigNodeFactory {

    private EnvironmentConfigNodeFactory() {

    }

    /**
     * Load a given {@link Properties} into a {@link ConfigNode}.
     *
     * @param input The properties to load.
     * @return The loaded {@code ConfigNode}.
     */
    public static ConfigNode load(Map<String, String> input) {
        ConfigNode config = new ConfigNode();

        load(config, input);

        return config;
    }

    protected static void load(ConfigNode config, Map<String, String> input) {
        Set<String> names = input.keySet();

        for (String name : names) {
            String after = normalizeName(name);
            if (after.startsWith("swarm.") || after.startsWith("thorntail.")) {
                String value = input.get(name);
                if (NullPlaceholder.VALUE.equals(value)) {
                    value = null;
                }
                ConfigKey key = ConfigKey.parse(after);
                config.recursiveChild(key, value);
            }
        }
    }

    protected static String normalizeName(String key) {
        key = key.replace("_DASH_", "-");
        key = key.replace("_UNDERSCORE_", "---");
        key = key.replace('_', '.');
        key = key.replace("---", "_");
        key = key.toLowerCase();
        return key;
    }
}


