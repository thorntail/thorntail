package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;

/**
 * @author Bob McWhirter
 */
public interface JARArchive extends
        Archive<JARArchive>,
        ServiceProviderContainer<JARArchive>,
        DependenciesContainer<JARArchive>,
        JBossDeploymentStructureContainer<JARArchive> {

}
