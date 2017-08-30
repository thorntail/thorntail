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

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
interface PropertiesManipulator {

    static PropertiesManipulator system() {
        return SystemPropertiesManipulator.INSTANCE;
    }

    static PropertiesManipulator forProperties(Properties properties) {
        return new ObjectPropertiesManipulator(properties);
    }

    String getProperty(String name);

    void setProperty(String name, String value);

    void clearProperty(String name);

    Properties getProperties();

    class SystemPropertiesManipulator implements PropertiesManipulator {

        private static SystemPropertiesManipulator INSTANCE = new SystemPropertiesManipulator();

        private SystemPropertiesManipulator() {

        }

        @Override
        public String getProperty(String name) {
            return System.getProperty(name);
        }

        @Override
        public void setProperty(String name, String value) {
            System.setProperty(name, value);
        }

        @Override
        public void clearProperty(String name) {
            System.clearProperty(name);
        }

        @Override
        public Properties getProperties() {
            return System.getProperties();
        }
    }

    class ObjectPropertiesManipulator implements PropertiesManipulator {

        private ObjectPropertiesManipulator(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String getProperty(String name) {
            return this.properties.getProperty(name);
        }

        @Override
        public void setProperty(String name, String value) {
            this.properties.setProperty(name, value);
        }

        @Override
        public void clearProperty(String name) {
            this.properties.remove(name);
        }

        @Override
        public Properties getProperties() {
            return this.properties;
        }

        private final Properties properties;
    }
}
