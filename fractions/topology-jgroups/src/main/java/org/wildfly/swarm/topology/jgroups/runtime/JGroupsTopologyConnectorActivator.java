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
package org.wildfly.swarm.topology.jgroups.runtime;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.swarm.topology.runtime.TopologyManager;
import org.wildfly.swarm.topology.runtime.TopologyManagerActivator;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class JGroupsTopologyConnectorActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        JGroupsTopologyConnector manager = new JGroupsTopologyConnector();

        target.addService(TopologyManagerActivator.CONNECTOR_SERVICE_NAME, manager)
                .addDependency(ServiceName.parse("org.wildfly.clustering.default-command-dispatcher-factory"), CommandDispatcherFactory.class, manager.getCommandDispatcherFactoryInjector())
                //.addDependency(ServiceName.parse("org.wildfly.network.socket-binding.http"), SocketBinding.class, manager.getSocketBindingInjector())
                .addDependency(TopologyManagerActivator.SERVICE_NAME, TopologyManager.class, manager.getTopologyManagerInjector())
                .install();

    }
}
