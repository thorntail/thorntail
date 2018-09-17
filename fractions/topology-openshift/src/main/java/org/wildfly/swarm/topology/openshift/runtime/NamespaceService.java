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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IProject;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class NamespaceService implements Service<String> {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("namespace");

    public Injector<IClient> getClientInjector() {
        return this.clientInjector;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.client = this.clientInjector.getValue();

        this.namespace = System.getenv("KUBERNETES_NAMESPACE");

        if (this.namespace == null) {
            Path namespaceFile = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
            if (Files.exists(namespaceFile)) {
                try {
                    this.namespace = new String(Files.readAllBytes(namespaceFile));
                } catch (IOException ignored) {
                    // shouldn't happen, this file is on tmpfs
                    // but if it happened anyway, we'll try the following options
                }
            }
        }

        if (this.namespace == null) {
            this.namespace = System.getenv("OPENSHIFT_BUILD_NAMESPACE");
        }

        if (this.namespace == null) {
            List<IProject> projects = this.client.list(ResourceKind.PROJECT);
            if (projects.size() != 1) {
                throw new StartException("Unable to automatically detect the " +
                                                 "Kubernetes namespace to use. Set the environment " +
                                                 "variable KUBERNETES_NAMESPACE and try again.");
            }
            this.namespace = projects.get(0).getNamespace().getNamespaceName();
        }
    }

    @Override
    public void stop(StopContext context) {
        this.client = null;
        this.namespace = null;
    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        return this.namespace;
    }

    private InjectedValue<IClient> clientInjector = new InjectedValue<>();

    private IClient client;

    private String namespace;
}
