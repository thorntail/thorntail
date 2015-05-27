package org.wildfly.swarm.runtime.msc;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.wildfly.swarm.msc.ServiceDeploymentRegistry;

import java.io.IOException;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ServiceInstanceActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        try {
            ServiceDeploymentRegistry registry = ServiceDeploymentRegistry.get();
            List<Service> instances = registry.getServices();
            int num = instances.size();

            for (int i = 0; i < num; ++i) {
                context.getServiceTarget().addService(ServiceName.of("wildfly", "swarm", System.getProperty("wildfly.swarm.current.deployment"), ""+ i), instances.get(i))
                        .install();
            }
        } catch (IOException e) {
            throw new ServiceRegistryException(e);
        }
    }
}
