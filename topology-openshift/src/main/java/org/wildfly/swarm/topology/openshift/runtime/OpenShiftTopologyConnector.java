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

import com.openshift.restclient.IClient;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.runtime.TopologyConnector;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/**
 * Topology connector for OpenShift 3.
 *
 * This topology connector knows how to interact with the Kubernetes
 * API in OpenShift 3 to provide topology information to other systems.
 * For now it is read-only, exposing Kubernetes Services but relying
 * on OpenShift methods to create and announces new services.
 *
 * The default service account for the OpenShift applications needs read
 * access to the Kubernetes API. This can be done with a command like:
 *
 * <code>oc policy add-role-to-user -z default view</code>
 *
 * Relevant environment variables:
 * <ul>
 * <li><code>$KUBERNETES_SERVICE_HOST</code> - the hostname of the Kube API
 * server, set automatically when running on OpenShift</li>
 * <li><code>$KUBERNETES_SERVICE_PORT</code> - the port of the Kube API
 * server, set automatically when running on OpenShift</li>
 * <li><code>$KUBERNETES_NAMESPACE</code> - the Kube namespace to use -
 * this is the project name when running on OpenShift. This will be
 * auto-detected in some cases and an error will be thrown if the value
 * can't be auto-detected and also is not set</li>
 * </ul>
 *
 * @author Ben Browning
 */
public class OpenShiftTopologyConnector implements Service<OpenShiftTopologyConnector>, TopologyConnector {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm.topology.openshift");

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ClientService clientService = new ClientService();
        target.addService(ClientService.SERVICE_NAME, clientService)
                .install();

        NamespaceService namespaceService = new NamespaceService();
        target.addService(NamespaceService.SERVICE_NAME, namespaceService)
                .addDependency(ClientService.SERVICE_NAME, IClient.class, namespaceService.getClientInjector())
                .install();

        ServiceWatcher watcher = new ServiceWatcher();
        target.addService(ServiceWatcher.SERVICE_NAME, watcher)
                .addDependency(ClientService.SERVICE_NAME, IClient.class, watcher.getClientInjector())
                .addDependency(NamespaceService.SERVICE_NAME, String.class, watcher.getNamespaceInjector())
                .addDependency(TopologyManager.SERVICE_NAME, TopologyManager.class, watcher.getTopologyManagerInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public void advertise(String name, SocketBinding binding, String... tags) {
        // no-op - let OpenShift handle it
    }

    @Override
    public void unadvertise(String name, SocketBinding binding) {
        // no-op - let OpenShift handle it
    }

    @Override
    public OpenShiftTopologyConnector getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Injector<TopologyManager> getTopologyManagerInjector() {
        return this.topologyManagerInjector;
    }

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();
}
