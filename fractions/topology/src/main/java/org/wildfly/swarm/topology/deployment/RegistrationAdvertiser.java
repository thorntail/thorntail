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
package org.wildfly.swarm.topology.deployment;

import javax.enterprise.inject.Vetoed;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.TopologyConnector;
import org.wildfly.swarm.topology.TopologyMessages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 * @author Bob McWhirter
 */
@Vetoed
public class RegistrationAdvertiser implements Service<Void> {

    public static final ServiceName CONNECTOR_SERVICE_NAME = ServiceName.of("swarm", "topology", "connector");

    public static ServiceController<Void> install(ServiceTarget target,
                                                  String serviceName,
                                                  String socketBindingName,
                                                  Collection<String> userDefinedTags) {
        List<String> tags = new ArrayList<>(userDefinedTags);
        tags.add(socketBindingName);

        ServiceName socketBinding = ServiceName.parse("org.wildfly.network.socket-binding." + socketBindingName);
        RegistrationAdvertiser advertiser = new RegistrationAdvertiser(serviceName, tags.toArray(new String[tags.size()]));

        return target.addService(ServiceName.of("swarm", "topology", "register", serviceName, socketBindingName), advertiser)
                .addDependency(CONNECTOR_SERVICE_NAME, TopologyConnector.class, advertiser.getTopologyConnectorInjector())
                .addDependency(socketBinding, SocketBinding.class, advertiser.getSocketBindingInjector())
                .setInitialMode(ServiceController.Mode.PASSIVE)
                .install();
    }

    RegistrationAdvertiser(String name, String... tags) {
        this.name = name;
        this.tags = tags;
    }

    Injector<TopologyConnector> getTopologyConnectorInjector() {
        return this.topologyConnectorInjector;
    }

    Injector<SocketBinding> getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        try {
            this.topologyConnectorInjector.getValue().advertise(this.name, this.socketBindingInjector.getValue(), this.tags);
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            this.topologyConnectorInjector.getValue().unadvertise(this.name, this.socketBindingInjector.getValue());
        } catch (Exception e) {
            TopologyMessages.MESSAGES.errorStoppingAdvertisement(e);
        }
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
