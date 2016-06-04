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
package org.wildfly.swarm.arquillian.resources;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.wildfly.swarm.spi.api.SwarmProperties;

public class SwarmURLResourceProvider extends OperatesOnDeploymentAwareProvider {
    @Override
    public boolean canProvide(Class<?> type) {
        return URL.class.isAssignableFrom(type);
    }

    @Override
    public Object doLookup(ArquillianResource resource, Annotation... __) {

        // first cut - try to get the data from the sysprops
        // this will fail if the user sets any of these via code
        String host = System.getProperty(SwarmProperties.BIND_ADDRESS);
        if (host == null || host.equals("0.0.0.0")) {
            host = "localhost";
        }

        int port = 8080;

        final String portString = System.getProperty(SwarmProperties.HTTP_PORT);
        if (portString != null) {
            port = Integer.parseInt(portString);
        }

        String contextPath = System.getProperty(SwarmProperties.CONTEXT_PATH);
        if (contextPath == null) {
            contextPath = "/";
        }

        try {
            return new URI("http", null, host, port, contextPath, null, null).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
