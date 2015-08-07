package org.wildfly.swarm.msc;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.wildfly.swarm.container.DependenciesContainer;
import org.wildfly.swarm.container.JBossDeploymentStructureContainer;

/**
 * @author Bob McWhirter
 */
public interface ServiceActivatorArchive extends Assignable {

    ServiceActivatorArchive addServiceActivator(Class<? extends ServiceActivator> cls);
    ServiceActivatorArchive addServiceActivator(String clsName);

}
