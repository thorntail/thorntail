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

import org.jboss.arquillian.container.test.impl.RemoteExtensionLoader;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.core.spi.ExtensionLoader;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.spi.api.JARArchive;

public class WildFlySwarmDeploymentAppender extends CachedAuxilliaryArchiveAppender {
    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(
                        true,
                        "org.wildfly.swarm.arquillian.adapter.resources")
                .addClass(WildFlySwarmRemoteExtension.class)
                .addClass(WildFlySwarmCommandService.class)
                .addClass(ServiceRegistryServiceActivator.class)
                .addAsServiceProvider(RemoteLoadableExtension.class, WildFlySwarmRemoteExtension.class)
                .addAsServiceProvider(ServiceActivator.class, ServiceRegistryServiceActivator.class)
                .addAsServiceProvider(ExtensionLoader.class, RemoteExtensionLoader.class);
    }
}
