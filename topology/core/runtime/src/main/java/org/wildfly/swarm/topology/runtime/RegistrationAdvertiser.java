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

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class RegistrationAdvertiser implements Service<Void> {

    public RegistrationAdvertiser(String name, String... tags) {
        this.name = name;
        this.tags = tags;
    }

    public Injector<TopologyConnector> getTopologyConnectorInjector() {
        return this.topologyConnectorInjector;
    }

    public Injector<SocketBinding> getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.topologyConnectorInjector.getValue().advertise(this.name, this.socketBindingInjector.getValue(), this.tags);
    }

    @Override
    public void stop(StopContext stopContext) {
        this.topologyConnectorInjector.getValue().unadvertise(this.name, this.socketBindingInjector.getValue());
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private final String name;

    private final String[] tags;

    private InjectedValue<TopologyConnector> topologyConnectorInjector = new InjectedValue<>();

    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<>();
}
