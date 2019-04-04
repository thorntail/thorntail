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
package org.wildfly.swarm.arquillian.adapter.resources;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.context.DeploymentContext;
import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.wildfly.swarm.spi.api.SwarmProperties;

public class SwarmURLResourceProvider extends OperatesOnDeploymentAwareProvider {

    @Inject
    private Instance<DeploymentContext> deploymentContext;

    @Inject
    Instance<Container> containerInstance;

    @Override
    public boolean canProvide(Class<?> type) {
        return URL.class.isAssignableFrom(type);
    }

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... __) {

        Container container = containerInstance.get();
        String javaVmArguments = null;
        String portDefinedInProperties = null;
        String offsetDefinedInProperties = null;
        if (container != null) {
            javaVmArguments = container.getContainerConfiguration().getContainerProperties().get("javaVmArguments");
        }
        if (javaVmArguments != null) {
            if (javaVmArguments.contains(SwarmProperties.HTTP_PORT) || javaVmArguments.contains(SwarmProperties.PORT_OFFSET)) {
                String[] properties = javaVmArguments.split("=| |\n");

                for (int i = 0; i < properties.length; i++) {
                    if (properties[i].contains(SwarmProperties.HTTP_PORT)) {
                        portDefinedInProperties = properties[i + 1];
                    }
                    if (properties[i].contains(SwarmProperties.PORT_OFFSET)) {
                        offsetDefinedInProperties = properties[i + 1];
                    }
                }
            }
        }
        // first cut - try to get the data from the sysprops
        // this will fail if the user sets any of these via code
        String host = System.getProperty(SwarmProperties.BIND_ADDRESS);
        if (host == null || host.equals("0.0.0.0")) {
            host = "localhost";
        }

        int port = 8080;

        final String portString = portDefinedInProperties != null ? portDefinedInProperties : System.getProperty(SwarmProperties.HTTP_PORT);
        final String portOffset = offsetDefinedInProperties != null ? offsetDefinedInProperties : System.getProperty(SwarmProperties.PORT_OFFSET);
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        if (portOffset != null) {
            port = port + Integer.parseInt(portOffset);
        }

        String contextPath = System.getProperty(SwarmProperties.CONTEXT_PATH);
        DeploymentContext deploymentContext = this.deploymentContext.get();
        if (deploymentContext != null && deploymentContext.isActive()) {
            if (deploymentContext.getObjectStore().get(ContextRoot.class) != null) {
                contextPath = deploymentContext.getObjectStore().get(ContextRoot.class).context();
            }
        }

        if (contextPath == null) {
            contextPath = "/";
        }

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        try {
            return new URI("http", null, host, port, contextPath, null, null).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
