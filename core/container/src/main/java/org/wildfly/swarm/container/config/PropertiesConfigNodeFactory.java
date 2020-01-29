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

import java.util.Enumeration;
import java.util.Properties;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.config.ConfigKey;

/**
 * Factory capable ofr building a {@code ConfigNode} tree from a {@link Properties} object.
 *
 * @author Bob McWhirter
 */
@Vetoed
public class PropertiesConfigNodeFactory {

    private PropertiesConfigNodeFactory() {

    }

    /**
     * Load a given {@link java.util.Properties} into a {@link ConfigNode}.
     *
     * @param input The properties to load.
     * @return The loaded {@code ConfigNode}.
     */
    public static ConfigNode load(Properties input) {
        ConfigNode config = new ConfigNode();

        load(config, input);

        return config;
    }

    protected static void load(ConfigNode config, Properties input) {
        Enumeration<?> names = input.propertyNames();

        while (names.hasMoreElements()) {
            String propName = names.nextElement().toString();
            String propValue = input.getProperty(propName);
            if (NullPlaceholder.VALUE.equals(propValue)) {
                propValue = null;
            }

            ConfigKey key = ConfigKey.parse(propName);

            config.recursiveChild(key, propValue);
        }
    }

}


