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
package org.wildfly.swarm.jmx.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.wildfly.swarm.config.ManagementCoreService;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.jmx.JMXRemotingConnector;
import org.wildfly.swarm.jmx.JMXFraction;
import org.wildfly.swarm.jmx.JMXProperties;
import org.wildfly.swarm.remoting.RemotingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * Picks and/or verifies the remote JMX connector to use.
 *
 * <p>If a user has not specified <b>any</b> {@link JMXFraction#jmxRemotingConnector(JMXRemotingConnector)},
 * then nothing is done as JMX remains purely available only inside the process.</p>
 *
 * <p>If a user has specified a generic, unconfigured {@link JMXRemotingConnector}, then his selector
 * will pick the "best" connector available, given the following priority:</p>
 *
 * <ul>
 * <li>Management Interface (typically port 9990) if <code>io.thorntail:management</code> is present</li>
 * <li>Remoting over HTTP (typically normal web-port, 8080) if <code>io.thorntail:undertow</code> is present</li>
 * <li>Else, the legacy remoting port (typically port 4777)</li>
 * </ul>
 *
 * <p>A user who does not explicitly configure a {@link JMXRemotingConnector} can still activate
 * a connector using configuation values (through properties or project-stages.yml), using
 * the key of <code>thorntail.jmx.remote</code></p>.
 *
 * <p>A non-null value (such as "true") will cause the above logic to be followed in selecting
 * an endpoint.  If the value is <code>management</code>, then the management endpoint will
 * be considered to be explicitly selected. If management is available but the user would
 * rather use the standard HTTP interface, then a value of <code>http></code> may be used.</p>
 *
 * <p>In the event a user has specifically {@link JMXRemotingConnector#useManagementEndpoint(Boolean)} to
 * <code>true<</code>, then in the event <code>io.thorntail:management</code> is not present,
 * then the remote connector for JMX will be <b>completely disabled</b>.</p>
 *
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class JMXRemotingConnectorEndpointSelector implements Customizer {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.jmx");

    @Inject
    private JMXFraction jmx;

    @Inject
    private Instance<ManagementCoreService> management;

    @Inject
    private Instance<Undertow> undertow;

    @Inject
    private RemotingFraction remoting;

    @Inject
    @ConfigurationValue(JMXProperties.REMOTE)
    private String remote;

    @Override
    public void customize() {
        JMXRemotingConnector remotingConnector = this.jmx.subresources().jmxRemotingConnector();
        if (remotingConnector == null) {
            if (this.remote == null) {
                LOG.info("JMX not configured for remote access");
                return;
            }

            this.jmx.jmxRemotingConnector();

            remotingConnector = this.jmx.subresources().jmxRemotingConnector();

            if (this.remote.equals("http")) {
                remotingConnector.useManagementEndpoint(false);
            } else if (this.remote.equals("management")) {
                remotingConnector.useManagementEndpoint(true);
            }
        }

        boolean requiresLegacyRemoting = false;

        if (remotingConnector.useManagementEndpoint() == null) {
            if (!this.management.isUnsatisfied()) {
                LOG.info("JMX configured for remote connector: implicitly using management interface");
                remotingConnector.useManagementEndpoint(true);
            } else if (!this.undertow.isUnsatisfied()) {
                LOG.info("JMX configured for remote connector: implicitly using standard interface");
                remotingConnector.useManagementEndpoint(false);
            } else {
                requiresLegacyRemoting = true;
            }
        } else if (remotingConnector.useManagementEndpoint() && this.management.isUnsatisfied()) {
            LOG.warn("JMX configured to use management endpoint, but io.thorntail:management not available. Disabling");
            this.jmx.jmxRemotingConnector(() -> null);
        } else if (this.undertow.isUnsatisfied()) {
            requiresLegacyRemoting = true;
        }

        if (requiresLegacyRemoting) {
            remotingConnector.useManagementEndpoint(false);
            LOG.info("JMX configured for remote connector but neither management nor http interfaces available. Using legacy remoting.");
            this.remoting.requireLegacyConnector(true);
        }
    }
}
