/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.util;

public class BootstrapProperties {

    public static final String BUNDLED_DEPENDENCIES = "swarm.bundled.dependencies";

    public static final String APP_NAME = "swarm.app.name";

    public static final String APP_PATH = "swarm.app.path";

    public static final String APP_ARTIFACT = "swarm.app.artifact";

    //public
    public static final String DEBUG_PORT = "swarm.debug.port";

    public static boolean flagIsSet(final String prop) {
        return flagIsSet(prop, false);
    }

    public static boolean flagIsSet(final String prop, final boolean defaultValue) {
        final String value = System.getProperty(prop);

        if (value != null) {

            return !"false".equals(value.toLowerCase());
        } else {

            return defaultValue;
        }
    }
}
