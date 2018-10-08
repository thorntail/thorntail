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
package org.wildfly.swarm.bootstrap.util;

public class BootstrapProperties {

    public static final String BUNDLED_DEPENDENCIES = "thorntail.bundled.dependencies";

    public static final String APP_NAME = "thorntail.app.name";

    public static final String APP_PATH = "thorntail.app.path";

    public static final String APP_ARTIFACT = "thorntail.app.artifact";

    public static final String DEFAULT_DEPLOYMENT_TYPE = "thorntail.default.deployment.type";

    public static final String IS_UBERJAR = "thorntail.isuberjar";

    private BootstrapProperties() {
    }

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
