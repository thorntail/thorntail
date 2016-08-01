/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.topology.webapp;

import java.util.Set;

import io.undertow.server.HttpHandler;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.TopologyConnector;

public class TopologyWebAppActivator implements ServiceActivator {

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
                                         HttpHandler.class, proxyService.getHandlerInjectorFor(serviceName));
        }
        serviceBuilder.install();
    }

    private final Set<String> serviceNames;
}
