/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
import org.wildfly.swarm.container.DependenciesContainer;
import org.wildfly.swarm.container.JBossDeploymentStructureContainer;
import org.wildfly.swarm.undertow.JBossWebContainer;
import org.wildfly.swarm.undertow.StaticContentContainer;

/**
 * @author Bob McWhirter
 */
public interface JAXRSArchive extends
        Archive<JAXRSArchive>,
        LibraryContainer<JAXRSArchive>,
        WebContainer<JAXRSArchive>,
        ResourceContainer<JAXRSArchive>,
        ServiceProviderContainer<JAXRSArchive>,
        JBossDeploymentStructureContainer<JAXRSArchive>,
        JBossWebContainer<JAXRSArchive>,
        DependenciesContainer<JAXRSArchive>,
        StaticContentContainer<JAXRSArchive> {

    JAXRSArchive addResource(Class<?> resource);
}
