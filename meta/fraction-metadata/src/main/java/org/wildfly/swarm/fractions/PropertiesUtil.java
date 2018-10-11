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
package org.wildfly.swarm.fractions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Toby Crawley
 */
public class PropertiesUtil {

    private PropertiesUtil() {
    }

    public static Properties loadProperties(final InputStream in) throws IOException {
        final Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }

        return props;
    }

    /**
     * Loads properties from the file identified by the given {@code fileString},
     * which can be a regular file (path), a classpath resource or a URL.
     *
     * @param fileString identifies the file
     * @return the properties
     * @throws IOException on errors reading the file/URL
     */
    public static Properties loadProperties(final String fileString) throws IOException {
        final File file = new File(fileString);
        // first try: regular file
        if (file.exists()) {
            return loadProperties(file);
        }
        // second try: classpath resource
        final InputStream resourceStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileString);
        if (resourceStream != null) {
            return loadProperties(resourceStream);
        }
        // third/last try: URL
        try {
            final URL url = new URL(fileString);
            return loadProperties(url.openStream());
        } catch (final MalformedURLException e) {
            // there is no guarantee that fileString is a URL at all
        }
        throw new IllegalArgumentException("Unable to find " + fileString);
    }

    public static Properties loadProperties(final File file) throws IOException {
        return loadProperties(new FileInputStream(file));
    }

    public static String versionFromPomProperties() {
        try {
            return loadProperties(PropertiesUtil.class
                                          .getClassLoader()
                                          .getResourceAsStream("META-INF/maven/org.wildfly.swarm/tools/pom.properties"))
                    .getProperty("version");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load version from pom.properties", e);
        }
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
                    key.startsWith("thorntail.") ||
                    key.startsWith("wildfly.") ||
                    (withMaven && key.startsWith("maven.")) ||
                    existing.containsKey(key)) {
                properties.put(key, System.getProperty(key));
            }
        });

        return properties;
    }
}
