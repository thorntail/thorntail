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
package org.wildfly.swarm.arquillian.daemon.container;

import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.wildfly.swarm.arquillian.daemon.protocol.DaemonProtocol;

/**
 * Base {@link LoadableExtension} implementation for Arquillian Server Daemon containers
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class DaemonContainerExtensionBase implements LoadableExtension {

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.core.spi.LoadableExtension#register(org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder)
     */
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(Protocol.class, DaemonProtocol.class);
    }

}
