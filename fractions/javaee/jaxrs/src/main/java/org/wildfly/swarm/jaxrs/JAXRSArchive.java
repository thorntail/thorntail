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
package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.wildfly.swarm.jaxrs.internal.JAXRSArchiveImpl;
import org.wildfly.swarm.spi.api.DependenciesContainer;
import org.wildfly.swarm.spi.api.JBossDeploymentStructureContainer;
import org.wildfly.swarm.spi.api.MarkerContainer;
import org.wildfly.swarm.undertow.StaticContentContainer;
import org.wildfly.swarm.undertow.descriptors.JBossWebContainer;
import org.wildfly.swarm.undertow.descriptors.WebXmlContainer;

/** An archive supporting easy usage of JAX-RS applications.
 *
 * <p>This archive type provides all capabilities of usual {@link .war} archives,
 * while also supporting the auto-creation of {@link javax.ws.rs.core.Application} class
 * if none is provided by the user.</p>
 *
 * @author Bob McWhirter
 */
public interface JAXRSArchive extends
        Archive<JAXRSArchive>,
        DependenciesContainer<JAXRSArchive>,
        LibraryContainer<JAXRSArchive>,
        WebContainer<JAXRSArchive>,
        ResourceContainer<JAXRSArchive>,
        ServiceProviderContainer<JAXRSArchive>,
        JBossDeploymentStructureContainer<JAXRSArchive>,
        JBossWebContainer<JAXRSArchive>,
        WebXmlContainer<JAXRSArchive>,
        MarkerContainer<JAXRSArchive>,
        StaticContentContainer<JAXRSArchive> {

    JAXRSArchive addResource(Class<?> resource);

    static boolean isJAXRS(Archive archive) {
        return JAXRSArchiveImpl.isJAXRS(archive);
    }
}
