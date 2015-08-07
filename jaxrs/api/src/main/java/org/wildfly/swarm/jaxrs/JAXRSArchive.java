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
