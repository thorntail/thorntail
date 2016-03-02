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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class ClientService implements Service<IClient> {

    public static final ServiceName SERVICE_NAME = OpenShiftTopologyConnector.SERVICE_NAME.append("client");

    public static int servicePort(String serviceName) {
        String envName = serviceName.replace("-", "_").toUpperCase() + "_SERVICE_PORT";
        String envPort = System.getenv(envName);
        if (envPort == null) {
            return -1;
        }

        return Integer.parseInt(envPort);
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.client = openshiftClient();
            // Trigger an API call to ensure we fail-fast if we can't talk
            // to the API
            this.client.getCurrentUser().getName();
        } catch (IOException ex) {
            throw new StartException(ex);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.client = null;
    }

    @Override
    public IClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }

    private IClient openshiftClient() throws IOException {
        String kubeHost = serviceHost("kubernetes");
        int kubePort = servicePort("kubernetes");

        Path tokenFile = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token");
        String scheme = "http";
        String token = null;
        if (Files.exists(tokenFile)) {
            token = new String(Files.readAllBytes(tokenFile));
            scheme = "https";
        }

        IClient client = new ClientFactory().create(scheme + "://" + kubeHost + ":" + kubePort, new NoopSSLCertificateCallback());
        if (token != null) {
            client.setAuthorizationStrategy(new TokenAuthorizationStrategy(token));
        }

        return client;
    }

    protected String serviceHost(String serviceName) {
        String envName = serviceName.replace("-", "_").toUpperCase() + "_SERVICE_HOST";
        return System.getenv(envName);
    }

    private IClient client;
}
