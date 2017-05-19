package org.wildfly.swarm.container.runtime.cdi;

import javax.enterprise.context.spi.AlterableContext;

import org.jboss.shrinkwrap.api.Archive;

/**
 * Created by bob on 5/12/17.
 */
public interface DeploymentContext extends AlterableContext {
    void activate(Archive<?> archive);
    void deactivate();

    Archive<?> getCurrentArchive();

}
