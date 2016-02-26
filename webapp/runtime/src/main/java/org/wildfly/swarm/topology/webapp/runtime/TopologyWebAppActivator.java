package org.wildfly.swarm.topology.webapp.runtime;

import io.undertow.server.handlers.proxy.ProxyHandler;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.runtime.TopologyConnector;

import java.util.Set;

public class TopologyWebAppActivator implements ServiceActivator {

    private final Set<String> serviceNames;

    public TopologyWebAppActivator(Set<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        TopologyProxyService proxyService = new TopologyProxyService(serviceNames);
        ServiceBuilder<TopologyProxyService> serviceBuilder = target
                .addService(TopologyProxyService.SERVICE_NAME, proxyService)
                .addDependency(TopologyConnector.SERVICE_NAME);
        for (String serviceName : serviceNames) {
            serviceBuilder.addDependency(proxyService.mscServiceNameForServiceProxy(serviceName),
                    ProxyHandler.class, proxyService.getHandlerInjectorFor(serviceName));
        }
        serviceBuilder.install();
    }
}
