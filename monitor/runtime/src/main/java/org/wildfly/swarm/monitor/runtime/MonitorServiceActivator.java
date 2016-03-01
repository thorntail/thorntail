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
package org.wildfly.swarm.monitor.runtime;

import org.jboss.as.controller.ModelController;
import org.jboss.as.naming.ImmediateManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.server.Services;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * Installs a monitoring service so we get hold of the MSC services
 * to access the management model and server environment.
 *
 * @see Monitor
 *
 * @author Heiko Braun
 */
public class MonitorServiceActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        MonitorService service = new MonitorService();

        target.addService(MonitorService.SERVICE_NAME, service)
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.serverEnvironmentValue)
                .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.modelControllerValue)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

        BinderService binderService = new BinderService(Monitor.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, Monitor.JNDI_NAME), binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(service))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();


    }
}