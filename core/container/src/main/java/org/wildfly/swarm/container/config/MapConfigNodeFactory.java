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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class MapConfigNodeFactory {

    private MapConfigNodeFactory() {

    }

    /**
     * Load a given {@link Map} into a {@link ConfigNode}.
     *
     * @param input The map to load.
     * @return The loaded {@code ConfigNode}.
     */
    public static ConfigNode load(Map<String, ?> input) {
        ConfigNode config = new ConfigNode();

        load(config, input);

        return config;
    }

    protected static void load(ConfigNode config, Map<String, ?> input) {
        Set<String> keys = input.keySet();

        for (String key : keys) {
            Object value = input.get(key);
            ConfigNode child = load(value);
            if (key.equals("swarm")) {
                key = "thorntail";
            }
            config.child(key, child);
        }
    }

    protected static void load(ConfigNode config, List<?> input) {
        int num = input.size();

        for (int i = 0; i < num; ++i) {
            Object value = input.get(i);
            ConfigNode child = load(value);
            config.child(new SimpleKey("" + i), child);
        }
    }

    @SuppressWarnings("unchecked")
    protected static ConfigNode load(Object value) {

        ConfigNode child = null;

        if (value instanceof Map) {
            child = new ConfigNode();
            load(child, (Map<String, ?>) value);
        } else if (value instanceof List) {
            child = new ConfigNode();
            load(child, (List<?>) value);
        } else if (value instanceof String) {
            child = new ConfigNode("" + value);
        } else if (value instanceof Long) {
            child = new ConfigNode("" + value);
        } else if (value instanceof Integer) {
            child = new ConfigNode("" + value);
        } else if (value instanceof Boolean) {
            child = new ConfigNode("" + value);
        } else if (value instanceof Float) {
            child = new ConfigNode("" + value);
        } else if (value instanceof Double) {
            child = new ConfigNode("" + value);
        }

        return child;

    }
}


