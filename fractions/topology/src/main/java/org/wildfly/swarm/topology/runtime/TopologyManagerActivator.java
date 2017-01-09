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
package org.wildfly.swarm.topology.runtime;

import javax.inject.Singleton;

import org.jboss.as.naming.ImmediateManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.wildfly.swarm.topology.Topology;

/**
 * @author Bob McWhirter
 */
@Singleton
public class TopologyManagerActivator implements ServiceActivator {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "topology");

    public static final ServiceName CONNECTOR_SERVICE_NAME = ServiceName.of("swarm", "topology", "connector");

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        TopologyManager.INSTANCE.setServiceTarget(target);

        target.addService(SERVICE_NAME, new ValueService<>(new ImmediateValue<>(TopologyManager.INSTANCE)))
                .install();

        BinderService binderService = new BinderService(Topology.JNDI_NAME, null, true);

        target.addService(ContextNames.buildServiceName(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, Topology.JNDI_NAME), binderService)
                .addDependency(ContextNames.JBOSS_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                .addInjection(binderService.getManagedObjectInjector(), new ImmediateManagedReferenceFactory(TopologyManager.INSTANCE))
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .install();

    }
}
