/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.runner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Thomas Petit, thomas.marc.petit@gmail.com
 * <br>
 * Date: 2/10/19
 */
public class RunnerConstants {
    public static final String CACHE_STORAGE_DIR;
    public static final String IGNORE_DEFAULT_REPOSITORIES;
    public static final String USER_REPOSITORIES;
    public static final String PRESERVE_JAR;

    static {
        Properties fileProperties = loadFileProperties();

        CACHE_STORAGE_DIR = getProperty(fileProperties, "thorntail.runner.cache-location", ".thorntail-runner-cache");
        IGNORE_DEFAULT_REPOSITORIES = getProperty(fileProperties, "thorntail.runner.ignore-default-repositories", null);
        USER_REPOSITORIES = getProperty(fileProperties, "thorntail.runner.repositories", null);
        PRESERVE_JAR = getProperty(fileProperties, "thorntail.runner.preserve-jar", null);
    }

    private RunnerConstants() {
    }

    private static Properties loadFileProperties() {
        Properties fileProperties = new Properties();
        try (InputStream st = new FileInputStream("thorntail-runner.properties")) {
            fileProperties.load(st);
        } catch (FileNotFoundException e) {
            System.out.println("No configuration (thorntail-runner.properties) found");
        } catch (IOException e) {
            System.out.println("Could not read configuration (thorntail-runner.properties), ignoring file");
        }
        return fileProperties;
    }

    /**
     * Load properties either from file or passed as system property. In case both are provided the system property will
     * overwrite the file based property.
     *
     * @param fileProperties Properties read from file
     * @param key            Property name that should be looked up in file or system properties
     * @param defaultValue   In case no property value was found we can define what should be returned
     * @return
     */
    private static String getProperty(Properties fileProperties, String key, String defaultValue) {
        String value = null;

        if (fileProperties.getProperty(key) != null) {
            value = fileProperties.getProperty(key);
        }
        if (System.getProperty(key) != null) {
            value = System.getProperty(key);
        }
        return value != null ? value : defaultValue;
    }
}
