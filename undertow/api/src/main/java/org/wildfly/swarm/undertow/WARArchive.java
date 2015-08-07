package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.wildfly.swarm.container.DependenciesContainer;
import org.wildfly.swarm.container.JBossDeploymentStructureContainer;

/**
 * @author Bob McWhirter
 */
public interface WARArchive extends
        Archive<WARArchive>,
        LibraryContainer<WARArchive>,
        WebContainer<WARArchive>,
        ResourceContainer<WARArchive>,
        ServiceProviderContainer<WARArchive>,
        JBossDeploymentStructureContainer<WARArchive>,
        JBossWebContainer<WARArchive>,
        DependenciesContainer<WARArchive>,
        StaticContentContainer<WARArchive> {

        }
