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
package org.wildfly.swarm.monitor.runtime;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.controller.ModelController;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.as.naming.ImmediateManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.monitor.MonitorFraction;

/**
 * Installs a monitoring service so we get hold of the MSC services
 * to access the management model and server environment.
 *
 * @author Heiko Braun
 * @see Monitor
 */
@ApplicationScoped
public class MonitorServiceActivator implements ServiceActivator {

    @Inject
    @Any
    Instance<MonitorFraction> monitorFractionInstance;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        Optional<String> securityRealm = Optional.empty();

        if (!monitorFractionInstance.isUnsatisfied()) {
            securityRealm = monitorFractionInstance.get().securityRealm();
        }

        ServiceTarget target = context.getServiceTarget();

        MonitorService service = new MonitorService(securityRealm);

        ServiceBuilder<MonitorService> monitorServiceServiceBuilder = target.addService(MonitorService.SERVICE_NAME, service);

        ServiceBuilder<MonitorService> serviceBuilder = monitorServiceServiceBuilder
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.getServerEnvironmentInjector())
                .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.getModelControllerInjector());

        if (securityRealm.isPresent()) { // configured through the fraction interface
            serviceBuilder.addDependency(
                    createRealmName(securityRealm.get()),
                    SecurityRealm.class,
                    service.getSecurityRealmInjector()
            );
        }

        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

        BinderService binderService = new BinderService(Monitor.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, Monitor.JNDI_NAME), binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(service))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

    }

    public static ServiceName createRealmName(String realmName) {
        return BASE_SERVICE_NAME.append(realmName);
    }

    private static final ServiceName BASE_SERVICE_NAME = ServiceName.JBOSS.append("server", "controller", "management", "security_realm");

}