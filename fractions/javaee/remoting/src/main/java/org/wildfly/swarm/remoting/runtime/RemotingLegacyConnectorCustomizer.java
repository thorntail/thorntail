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
package org.wildfly.swarm.remoting.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.remoting.RemotingFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * Configures the legacy remoting socketing binding and connector if required.
 *
 * <p>In the event {@link RemotingFraction#requireLegacyConnector(boolean)}</p> has been
 * set to <code>true</code> or if configuration property <code>thorntail.remoting.port</code>
 * is set to any value, this customizer will install a socket-binding named
 * <code>legacy-remoting</code> for port <code>4447</code> or whatever value
 * configuration property <code>thorntail.remoting.port</code> is set to.</p>
 *
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class RemotingLegacyConnectorCustomizer implements Customizer {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.remoting");

    @Inject
    private RemotingFraction remoting;

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Override
    public void customize() {
        if (this.remoting.isRequireLegacyConnector()) {
            LOG.info("Remoting installed but Undertow not available. Enabled legacy connector on port 4447.");
            this.remoting.connector("legacy", (connector) -> {
                connector.socketBinding("legacy-remoting");
            });
            group.socketBinding(new SocketBinding("legacy-remoting").port(this.remoting.port()));
        }
    }
}
