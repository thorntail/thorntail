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
package org.wildfly.swarm.arquillian.adapter;

import org.jboss.arquillian.container.test.impl.enricher.resource.ContainerURIResourceProvider;
import org.jboss.arquillian.container.test.impl.enricher.resource.ContainerURLResourceProvider;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.wildfly.swarm.arquillian.adapter.resources.ServiceRegistryResourceProvider;
import org.wildfly.swarm.arquillian.adapter.resources.SwarmURIResourceProvider;
import org.wildfly.swarm.arquillian.adapter.resources.SwarmURLResourceProvider;

public class WildFlySwarmRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.override(ResourceProvider.class, ContainerURLResourceProvider.class, SwarmURLResourceProvider.class)
                .override(ResourceProvider.class, ContainerURIResourceProvider.class, SwarmURIResourceProvider.class)
                .service(ResourceProvider.class, ServiceRegistryResourceProvider.class)
                .service(CommandService.class, WildFlySwarmCommandService.class);
    }
}
