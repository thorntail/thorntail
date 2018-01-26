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
package org.wildfly.swarm.jose.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.as.naming.ImmediateManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.jose.JoseFraction;
import org.wildfly.swarm.jose.JoseLookup;

/**
 *
 */
@ApplicationScoped
public class JoseServiceActivator implements ServiceActivator {

    @Inject
    Instance<JoseFraction> joseFractionInstance;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        ServiceTarget target = context.getServiceTarget();

        JoseService service = new JoseService(joseFractionInstance.get().getJoseInstance());

        ServiceBuilder<JoseService> serviceBuilder = target.addService(JoseService.SERVICE_NAME, service);

        serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();

        BinderService binderService = new BinderService(JoseLookup.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, JoseLookup.JNDI_NAME), binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(service))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

    }
}
