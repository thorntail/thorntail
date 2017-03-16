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
package org.wildfly.swarm.topology.webapp.runtime;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.configuration.ReverseProxy;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.server.host.Location;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.topology.webapp.TopologyWebAppFraction;
import org.wildfly.swarm.undertow.UndertowFraction;

import static org.wildfly.swarm.topology.webapp.TopologyWebAppFraction.proxyHandlerName;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class TopologyProxiedServiceCustomizer implements Customizer {

    @SuppressWarnings("unused")
    @Inject @Any
    private UndertowFraction undertow;

    @SuppressWarnings("unused")
    @Inject @Any
    private TopologyWebAppFraction fraction;

    public void customize() {
        Map<String,String> mappings = this.fraction.proxiedServiceMappings();
        if (!mappings.isEmpty()) {
            HandlerConfiguration handlerConfig = undertow.subresources().handlerConfiguration();
            for (String serviceName : mappings.keySet()) {
                ReverseProxy<?> proxy = new ReverseProxy<>(proxyHandlerName(serviceName)).hosts(Collections.emptyList());
                handlerConfig.reverseProxy(proxy);

                String contextPath = mappings.get(serviceName);
                for (Server server : undertow.subresources().servers()) {
                    Location location = new Location(contextPath).handler(proxyHandlerName(serviceName));
                    for (Host host : server.subresources().hosts()) {
                        host.location(location);
                    }
                }
            }
        }
    }
}
