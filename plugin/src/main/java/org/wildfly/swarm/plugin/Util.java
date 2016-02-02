/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Util {

    public static Properties loadProperties(final InputStream in) throws IOException {
        final Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }

        return props;
    }

    public static Properties loadProperties(final String file) throws IOException {
        return loadProperties(new File(file));
    }

    public static Properties loadProperties(final File file) throws IOException {
        return loadProperties(new FileInputStream(file));
    }

    /**
     * Copies any jboss.*, swarm.*, or wildfly.* (and optionally maven.*) sysprops from System,
     * along with anything that shadows a specified property.
     *
     * @return only the filtered properties, existing is unchanged
     */
    public static Properties filteredSystemProperties(final Properties existing, final boolean withMaven) {
        final Properties properties = new Properties();

        System.getProperties().stringPropertyNames().forEach(key -> {
            if (key.startsWith("jboss.") ||
                    key.startsWith("swarm.") ||
                    key.startsWith("wildfly.") ||
                    (withMaven && key.startsWith("maven.")) ||
                    existing.containsKey(key)) {
                properties.put(key, System.getProperty(key));
            }
        });

        return properties;
    }
}
