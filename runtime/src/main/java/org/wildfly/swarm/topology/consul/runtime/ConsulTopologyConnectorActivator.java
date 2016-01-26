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
package org.wildfly.swarm.topology.consul.runtime;

import java.net.URL;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.runtime.TopologyConnector;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/** MSC activator for the ConsulTopologyConnector.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ConsulTopologyConnectorActivator implements ServiceActivator {

    private final URL url;

    public ConsulTopologyConnectorActivator(URL url) {
        this.url = url;
    }

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget target = context.getServiceTarget();

        ConsulTopologyConnector connector = new ConsulTopologyConnector( this.url );

        target.addService(TopologyConnector.SERVICE_NAME, connector)
                .addDependency(ServiceName.parse("org.wildfly.network.socket-binding.http"), SocketBinding.class, connector.getSocketBindingInjector())
                .addDependency(TopologyManager.SERVICE_NAME, TopologyManager.class, connector.getTopologyManagerInjector())
                .install();
    }
}
