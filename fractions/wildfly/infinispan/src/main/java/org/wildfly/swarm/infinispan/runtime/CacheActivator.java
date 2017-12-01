package org.wildfly.swarm.infinispan.runtime;

import org.jboss.msc.service.AbstractService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;

/** Activator to force a cache to start prior to deployment.
 * Created by bob on 8/15/17.
 */
public class CacheActivator implements ServiceActivator {

    private ServiceName BASE = ServiceName.parse("org.wildfly.clustering.infinispan.cache-container-configuration");

    public CacheActivator(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        Service<Void> service = new AbstractService<Void>() { };
        context.getServiceTarget().addService(BASE.append(this.cacheName).append("activator"), service)
                .addDependency(BASE.append(this.cacheName))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }

    private final String cacheName;
}
