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
package org.wildfly.swarm.spi.api;

/**
 * This class defines all system properties used by Swarm
 */
public interface SwarmProperties {

    /**
     * Causes a deployed artifact to be dumped to disk when swarm starts, for debugging. Defaults to <code>false</code>
     */
    String EXPORT_DEPLOYMENT = "thorntail.export.deployment";

    /**
     * Sets a global port adjustment, defaults to 0
     */
    String PORT_OFFSET = "thorntail.port.offset";

    /**
     * Interface to bind servers, defaults to 0.0.0.0
     */
    String BIND_ADDRESS = "thorntail.bind.address";

    /**
     * Since introducing the delayed open HTTP listeners, it has been determined
     * that maybe it'd be useful to be able to eagerly open the listeners through usage of a property.
     * If non-<code>null</code>, will cause the http listeners to not be lazy.
     */
    String HTTP_EAGER = "thorntail.http.eager";

    /**
     * Sets the HTTP port to be used, defaults to 8080
     */
    String HTTP_PORT = "thorntail.http.port";

    /**
     * Sets the AJP port to be used, defaults to 8009
     */
    String AJP_PORT = "thorntail.ajp.port";

    /**
     * Sets the HTTPS port to be used, defaults to 8443
     */
    String HTTPS_PORT = "thorntail.https.port";

    /**
     * If true, generates a self-signed certificate for development purposes
     */
    String HTTPS_GENERATE_SELF_SIGNED_CERTIFICATE = "thorntail.https.certificate.generate";

    /**
     * The host used in the self-signed certificate if {@link SwarmProperties#HTTPS_GENERATE_SELF_SIGNED_CERTIFICATE} is true
     * Defaults to localhost
     */
    String HTTPS_GENERATE_SELF_SIGNED_CERTIFICATE_HOST = "thorntail.https.certificate.generate.host";

    /**
     * The context path to be used, defaults to /
     */
    String CONTEXT_PATH = "thorntail.context.path";

    /**
     * If provided, the swarm process will pause for debugging on the given port.
     * This option is only available when running an Arquillian test or mvn wildfly-swarm:run, not when executing java -jar.
     * The latter requires normal Java debug agent parameters.
     */
    String DEBUG_PORT = "thorntail.debug.port";

    /**
     * If provided, the swarm container will log bootstrap information
     */
    String DEBUG_BOOTSTRAP = "thorntail.debug.bootstrap";

    /**
     * The environment this process is running on (eg. openshift)
     */
    String ENVIRONMENT = "thorntail.environment";

    /**
     * Activates a stage in project-stages.yml
     */
    String PROJECT_STAGE = "thorntail.project.stage";

    /**
     * A URL spec reference to a project stage file
     *
     * @see java.net.URL
     */
    String PROJECT_STAGE_FILE = "thorntail.project.stage.file";

    /**
     * Timeout, in seconds, to wait for a deployment to occur, defaults to 300
     */
    String DEPLOYMENT_TIMEOUT = "thorntail.deployment.timeout";

    /**
     * Port number for Swarm's Arquillian Daemon.
     */
    String ARQUILLIAN_DAEMON_PORT = "thorntail.arquillian.daemon.port";

    /**
     * Formats a property as ${property}
     *
     * @param property the property to be formatted
     * @return the formatted value as ${property}
     */
    static String propertyVar(final String property) {
        return String.format("${%s}", property);
    }

    /**
     * Formats a property as ${property:defaultValue}
     *
     * @param property     the property to be formatted
     * @param defaultValue
     * @return the formatted value as ${property:defaultValue}
     */
    static String propertyVar(final String property, final String defaultValue) {
        return String.format("${%s:%s}", property, defaultValue);
    }

}

