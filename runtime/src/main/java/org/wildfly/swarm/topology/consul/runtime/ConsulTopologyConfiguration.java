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

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.ServiceActivator;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.topology.consul.ConsulTopologyFraction;

/**
 * Server configuration for the ConsulTopologyFraction.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ConsulTopologyConfiguration extends AbstractServerConfiguration<ConsulTopologyFraction> {

    public ConsulTopologyConfiguration() {
        super(ConsulTopologyFraction.class);
    }

    @Override
    public List<ServiceActivator> getServiceActivators(ConsulTopologyFraction fraction) {
        List<ServiceActivator> activators = new ArrayList<>();
        activators.add(new ConsulTopologyConnectorActivator(fraction.url()));
        return activators;
    }

}
