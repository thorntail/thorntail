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
package org.wildfly.swarm.topology.openshift.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.IServicePort;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.runtime.Registration;
import org.wildfly.swarm.topology.runtime.TopologyManager;

public class ServiceWatcher implements Service<ServiceWatcher>, Runnable {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("service-watcher");

    public static final int DEFAULT_HTTPS_PORT = 8443;

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
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void stop(StopContext context) {
        this.thread.interrupt();
    }

    @Override
    public ServiceWatcher getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void run() {
        IClient client = this.clientInjector.getValue();
        String namespace = this.namespaceInjector.getValue();
        TopologyManager topologyManager = this.topologyManagerInjector.getValue();

        while (!Thread.currentThread().isInterrupted()) {
            // TODO: Move from polling to the OpenShift watch API
            // openshift-restclient-java first needs to publish a version supporting watch
            List<IService> services = client.list(ResourceKind.SERVICE, namespace);
            for (IService service : services) {
                Set<Registration> previousEntries = topologyManager.registrationsForService(service.getName());
                Set<Registration> newEntries = registrationsForService(service);

                previousEntries.stream()
                        .filter(e -> !newEntries.contains(e))
                        .forEach(topologyManager::unregister);

                newEntries.stream()
                        .filter(e -> !previousEntries.contains(e))
                        .forEach(topologyManager::register);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private Set<Registration> registrationsForService(IService service) {
        Set<Registration> newEntries = new HashSet<>();
        for (IServicePort servicePort : service.getPorts()) {
            // Only expose the service's default port and anything running on the https port
            if (servicePort.getPort() == service.getPort() || servicePort.getPort() == DEFAULT_HTTPS_PORT) {
                Registration registration = new Registration("openshift",
                                                             service.getName(),
                                                             service.getPortalIP(),
                                                             servicePort.getPort());
                if (servicePort.getPort() == DEFAULT_HTTPS_PORT) {
                    registration.addTags(Collections.singletonList("https"));
                } else if (servicePort.getPort() == service.getPort()) {
                    registration.addTags(Collections.singletonList("http"));
                }
                newEntries.add(registration);
            }
        }
        return newEntries;
    }

    private InjectedValue<IClient> clientInjector = new InjectedValue<>();

    private InjectedValue<String> namespaceInjector = new InjectedValue<>();

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();

    private Thread thread;
}
