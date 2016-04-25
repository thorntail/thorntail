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
package org.wildfly.swarm.spi.api;

public class SwarmProperties {
    public static final String VERSION = "swarm.version";

    //public
    public static final String EXPORT_DEPLOYMENT = "swarm.export.deployment";

    //public
    public static final String BUILD_MODULES = "swarm.build.modules";

    //public
    public static final String BUILD_REPOS = "swarm.build.repos";

    //public
    public static final String EXPORT_UBERJAR = "swarm.export.uberjar";

    public static final String CURRENT_DEPLOYMENT = "swarm.current.deployment";

    //public
    public static final String NODE_ID = "swarm.node.id";

    //public
    public static final String PORT_OFFSET = "swarm.port.offset";

    //public
    public static final String BIND_ADDRESS = "swarm.bind.address";

    //public
    public static final String HTTP_EAGER = "swarm.http.eager";

    //public
    public static final String ENVIRONMENT = "swarm.environment";

    /**
     * Full qualified http address, i.e. 'http://localhost:8500/
     */
    public static final String CONSUL_URL = "swarm.consul.url";


    public static String propertyVar(final String prop) {
        return String.format("${%s}", prop);
    }

    public static String propertyVar(final String prop, final String defaultValue) {
        return String.format("${%s:%s}", prop, defaultValue);
    }

}

