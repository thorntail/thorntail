package org.wildfly.swarm.arquillian.adapter;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceRegistryException;

/**
 * @author Bob McWhirter
 */
public class ServiceRegistryServiceActivator implements ServiceActivator {
    public static ServiceRegistry INSTANCE = null;
    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        INSTANCE = context.getServiceRegistry();
    }
}
