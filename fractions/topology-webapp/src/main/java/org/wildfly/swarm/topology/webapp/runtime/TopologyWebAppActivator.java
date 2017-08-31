/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.topology.webapp.runtime;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.undertow.server.HttpHandler;
import org.jboss.as.naming.service.DefaultNamespaceContextSelectorService;
import org.jboss.as.naming.service.NamingService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.runtime.TopologyManagerActivator;
import org.wildfly.swarm.topology.webapp.TopologyWebAppFraction;

@ApplicationScoped
public class TopologyWebAppActivator implements ServiceActivator {

    @Inject
    @Any
    private Instance<TopologyWebAppFraction> topologyWebAppFractionInstance;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        if (!topologyWebAppFractionInstance.isUnsatisfied()) {
            serviceNames = topologyWebAppFractionInstance.get().proxiedServiceMappings().keySet();
        }

        TopologyProxyService proxyService = new TopologyProxyService(serviceNames);
        ServiceBuilder<TopologyProxyService> serviceBuilder = target
                .addService(TopologyProxyService.SERVICE_NAME, proxyService)
                .addDependency(DefaultNamespaceContextSelectorService.SERVICE_NAME)
                .addDependency(TopologyManagerActivator.CONNECTOR_SERVICE_NAME)
                .addDependency(NamingService.SERVICE_NAME);

        for (String serviceName : serviceNames) {
            serviceBuilder.addDependency(proxyService.mscServiceNameForServiceProxy(serviceName),
                                         HttpHandler.class, proxyService.getHandlerInjectorFor(serviceName));
        }
        serviceBuilder.install();
    }

    private Set<String> serviceNames = Collections.emptySet();
}
