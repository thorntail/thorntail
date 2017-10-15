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
package org.wildfly.swarm.topology.openshift.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.openshift.restclient.IClient;
import com.openshift.restclient.IOpenShiftWatchListener;
import com.openshift.restclient.IWatcher;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.runtime.Registration;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ServiceWatcher implements Service<ServiceWatcher>, IOpenShiftWatchListener {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("service-watcher");

    public static final int DEFAULT_HTTPS_PORT = 8443;

    private static final String TOPOLOGY_SOURCE_KEY = "openshift";

    public Injector<IClient> getClientInjector() {
        return this.clientInjector;
    }

    public Injector<String> getNamespaceInjector() {
        return this.namespaceInjector;
    }

    public Injector<TopologyManager> getTopologyManagerInjector() {
        return this.topologyManagerInjector;
    }

    @Override
    public void start(StartContext context) throws StartException {
        startWatcher();
    }

    @Override
    public void stop(StopContext context) {
        if (openShiftWatcher != null) {
            openShiftWatcher.stop();
        }
    }

    @Override
    public ServiceWatcher getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    private void startWatcher() {
        IClient client = clientInjector.getValue();
        listenerState.set(ListenerState.STARTING);
        openShiftWatcher = client.watch(namespaceInjector.getValue(), this, ResourceKind.SERVICE);
    }

    @Override
    public void connected(List<IResource> resources) {
        this.listenerState.set(ListenerState.CONNECTED);

        resources.stream()
                .filter(p -> p.getKind().equals(ResourceKind.SERVICE))
                .forEach(r -> {
                    Set<Registration> regs = registrationsForService((IService) r);
                    regs.forEach(topologyManagerInjector.getValue()::register);
                });
    }

    @Override
    public void disconnected() {
        if (listenerState.get().equals(ListenerState.RESTARTING)) {
            // Restarting so ignore disconnect
            return;
        }

        // De-register all endpoints
        topologyManagerInjector.getValue().unregisterAll(TOPOLOGY_SOURCE_KEY);

        listenerState.set(ListenerState.DISCONNECTED);
    }

    @Override
    public void received(IResource resource, ChangeType change) {
        if (change.equals(ChangeType.ADDED)) {
            // Add new Service to topology
            IService service = (IService) resource;

            registrationsForService(service)
                    .forEach(topologyManagerInjector.getValue()::register);
        } else if (change.equals(ChangeType.DELETED)) {
            // Remove Service from topology
            topologyManagerInjector.getValue().unregisterAll(TOPOLOGY_SOURCE_KEY, resource.getName());
        }
    }

    @Override
    public void error(Throwable err) {
        // Log the error ??
        restart();
    }

    private void restart() {
        switch (listenerState.get()) {
            case STARTING:
//                Trace.debug("Returning early from restart.  Already starting for project {0} and kind {1}", project.getName(), kind);
            case DISCONNECTED:
//                Trace.debug("Endpoint disconnected and skipping restart for project {0} and kind {1}", project.getName(), kind);
                return;
            default:
        }

        if (openShiftWatcher != null) {
            listenerState.set(ListenerState.RESTARTING);
            openShiftWatcher.stop();
        }

        startWatcher();
    }

    private Set<Registration> registrationsForService(IService service) {
        Set<Registration> newEntries = new HashSet<>();
        // Only expose the service's default port and anything running on the https port
        service.getPorts()
                .stream()
                .filter(servicePort -> servicePort.getPort() == service.getPort() || servicePort.getPort() == DEFAULT_HTTPS_PORT)
                .forEach(servicePort -> {
                    Registration registration = new Registration(TOPOLOGY_SOURCE_KEY,
                                                                 service.getName(),
                                                                 service.getClusterIP(),
                                                                 servicePort.getPort());
                    if (servicePort.getPort() == DEFAULT_HTTPS_PORT) {
                        registration.addTags(Collections.singletonList("https"));
                    } else if (servicePort.getPort() == service.getPort()) {
                        registration.addTags(Collections.singletonList("http"));
                    }
                    newEntries.add(registration);
                });
        return newEntries;
    }

    private enum ListenerState {
        STARTING,
        CONNECTED,
        RESTARTING,
        DISCONNECTED
    }

    private InjectedValue<IClient> clientInjector = new InjectedValue<>();

    private InjectedValue<String> namespaceInjector = new InjectedValue<>();

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();

    private IWatcher openShiftWatcher;

    private AtomicReference<ListenerState> listenerState = new AtomicReference<>(ListenerState.DISCONNECTED);
}
