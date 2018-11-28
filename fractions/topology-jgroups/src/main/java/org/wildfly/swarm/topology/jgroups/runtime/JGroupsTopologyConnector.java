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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherFactory;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;
import org.wildfly.swarm.topology.TopologyConnector;
import org.wildfly.swarm.topology.TopologyMessages;
import org.wildfly.swarm.topology.runtime.Registration;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/**
 * @author Bob McWhirter
 */
public class JGroupsTopologyConnector implements Service<JGroupsTopologyConnector>, Group.Listener, TopologyConnector {

    public JGroupsTopologyConnector() {
    }

    public Injector<CommandDispatcherFactory> getCommandDispatcherFactoryInjector() {
        return this.commandDispatcherFactoryInjector;
    }

    public Injector<TopologyManager> getTopologyManagerInjector() {
        return this.topologyManagerInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.commandDispatcherFactoryInjector.getValue().getGroup().addListener(this);
        this.dispatcher = this.commandDispatcherFactoryInjector.getValue().createCommandDispatcher("netflix.runtime.manager", this);
        this.node = this.commandDispatcherFactoryInjector.getValue().getGroup().getLocalNode();
        try {
            requestAdvertisements();
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        this.dispatcher.close();
    }

    @Override
    public JGroupsTopologyConnector getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void membershipChanged(List<Node> previousMembers, List<Node> members, boolean merged) {
        try {
            advertiseAll();
        } catch (Exception e) {
            TopologyMessages.MESSAGES.errorStartingAdvertisement(e);
        }
        List<Node> removed = new ArrayList<>();
        removed.addAll(previousMembers);
        removed.removeAll(members);
        removed.forEach((e) -> {
            this.topologyManagerInjector.getValue().unregisterAll(sourceKey(e));
        });
    }

    public synchronized void advertise(String name, SocketBinding binding, String... tags) throws Exception {
        Registration registration = new Registration(sourceKey(this.node), name, binding.getAddress().getHostAddress(), binding.getAbsolutePort(), tags);

        this.registrations.put(name + ":" + binding.getName(), registration);
        advertise(registration);
    }

    public synchronized void advertise(Registration registration) throws Exception {
        this.topologyManagerInjector.getValue().register(registration);
        doAdvertise(registration);
    }

    public synchronized void unadvertise(String appName, SocketBinding binding) throws Exception {
        Registration registration = this.registrations.remove(appName + ":" + binding.getName());
        if (registration != null) {
            this.dispatcher.executeOnGroup(new UnadvertiseCommand(registration));
        }
    }

    protected void requestAdvertisements() throws Exception {
        this.dispatcher.executeOnGroup(new RequestAdvertisementsCommand(), this.node);
    }

    protected synchronized void advertiseAll() throws Exception {
        for (Registration each : this.topologyManagerInjector.getValue().registrationsForSourceKey(sourceKey(this.node))) {
            doAdvertise(each);
        }
    }

    protected void doAdvertise(Registration registration) throws Exception {
        this.dispatcher.executeOnGroup(new AdvertiseCommand(registration));
    }

    void register(Registration registration) {
        this.topologyManagerInjector.getValue().register(registration);
    }

    void unregister(Registration registration) {
        this.topologyManagerInjector.getValue().unregister(registration);
    }

    String sourceKey(Node node) {
        return node.getName() + ":" + node.getSocketAddress().toString();

    }

    private InjectedValue<CommandDispatcherFactory> commandDispatcherFactoryInjector = new InjectedValue<>();

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();

    private CommandDispatcher<JGroupsTopologyConnector> dispatcher;

    private Node node;

    private Map<String, Registration> registrations = new ConcurrentHashMap<>();

}
