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

/**
 * This class defines all system properties used by Swarm
 */
public class SwarmProperties {

    /**
     * Internal property
     */
    public static final String VERSION = "swarm.version";

    /**
     * Causes a deployed artifact to be dumped to disk when swarm starts, for debugging. Defaults to <code>false</code>
     */
    public static final String EXPORT_DEPLOYMENT = "swarm.export.deployment";

    /**
     * Internal property
     */
    public static final String BUILD_MODULES = "swarm.build.modules";

    /**
     * Internal property
     */
    public static final String BUILD_REPOS = "swarm.build.repos";

    /**
     * Internal property
     */
    public static final String EXPORT_UBERJAR = "swarm.export.uberjar";

    /**
     * Internal property
     */
    public static final String CURRENT_DEPLOYMENT = "swarm.current.deployment";

    /**
     * Internal property
     */
    public static final String NODE_ID = "swarm.node.id";

    /**
     * Sets a global port adjustment, defaults to 0
     */
    public static final String PORT_OFFSET = "swarm.port.offset";

    /**
     * Interface to bind servers, defaults to 0.0.0.0
     */
    public static final String BIND_ADDRESS = "swarm.bind.address";

    /**
     * Since introducing the delayed open HTTP listeners, it has been determined
     * that maybe it'd be useful to be able to eagerly open the listeners through usage of a property.
     * If non-<code>null</code>, will cause the http listeners to not be lazy.
     */
    public static final String HTTP_EAGER = "swarm.http.eager";

    /**
     * Sets the HTTP port to be used, defaults to 8080
     */
    public static final String HTTP_PORT = "swarm.http.port";

    /**
     * Sets the HTTPS port to be used, defaults to 8443
     */
    public static final String HTTPS_PORT = "swarm.https.port";

    /**
     * The context path to be used, defaults to /
     */
    public static final String CONTEXT_PATH = "swarm.context.path";

    /**
     * If provided, the swarm process will pause for debugging on the given port.
     * This option is only available when running an Arquillian test or mvn wildfly-swarm:run, not when executing java -jar.
     * The latter requires normal Java debug agent parameters.
     */
    public static final String DEBUG_PORT = "swarm.debug.port";

    /**
     * If provided, the swarm container will log bootstrap information
     */
    public static final String DEBUG_BOOTSTRAP= "swarm.debug.bootstrap";

    /**
     * The environment this process is running on (eg. openshift)
     */
    public static final String ENVIRONMENT = "swarm.environment";

    /**
     * Activates a stage in project-stages.yml
     */
    public static final String PROJECT_STAGE = "swarm.project.stage";

    /**
     * Full qualified http address, i.e. 'http://localhost:8500/
     */
    public static final String CONSUL_URL = "swarm.consul.url";

    /**
     * Timeout, in seconds, to wait for a deployment to occur, defaults to 300
     */
    public static final String DEPLOYMENT_TIMEOUT = "swarm.deployment.timeout";
    
    /**
     * Formats a property as ${property}
     *
     * @param property the property to be formatted
     * @return the formatted value as ${property}
     */
    public static String propertyVar(final String property) {
        return String.format("${%s}", property);
    }

    /**
     * Formats a property as ${property:defaultValue}
     *
     * @param property     the property to be formatted
     * @param defaultValue
     * @return the formatted value as ${property:defaultValue}
     */
    public static String propertyVar(final String property, final String defaultValue) {
        return String.format("${%s:%s}", property, defaultValue);
    }

}

