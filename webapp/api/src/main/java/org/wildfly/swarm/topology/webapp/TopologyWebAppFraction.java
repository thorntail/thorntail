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
package org.wildfly.swarm.topology.webapp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.configuration.ReverseProxy;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.server.host.Location;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Lance Ball
 */
public class TopologyWebAppFraction implements Fraction {

    /**
     * @param serviceName
     * @return the name of the Undertow proxy handler for this service
     */
    public static String proxyHandlerName(String serviceName) {
        return serviceName + "-proxy-handler";
    }

    /**
     * Set up a load-balancing reverse proxy for the given service at the
     * given context path. Requests to this proxy will be load-balanced
     * among all instances of the service, as provided by our Topology.
     *
     * @param serviceName the name of the service to proxy
     * @param contextPath the context path expose the proxy under
     */
    public void proxyService(String serviceName, String contextPath) {
        if (proxiedServiceMappings().containsValue(contextPath)) {
            throw new IllegalArgumentException("Cannot proxy multiple services under the same context path");
        }
        proxiedServiceMappings.put(serviceName, contextPath);
    }

    /**
     * Get a map, keyed by service name, of the proxied service names
     * and their context paths.
     *
     * @return the map of proxied services and their context paths
     */
    public Map<String, String> proxiedServiceMappings() {
        return proxiedServiceMappings;
    }

    /**
     * Set to true to expose a Topology SSE endpoint and topology.js for
     * consuming topology information from the browser. Set to false to
     * disable this endpoint.
     *
     * Defaults to true.
     *
     * @param exposeTopologyEndpoint whether to expose the endpoint or not
     */
    public void exposeTopologyEndpoint(boolean exposeTopologyEndpoint) {
        this.exposeTopologyEndpoint = exposeTopologyEndpoint;
    }

    public boolean exposeTopologyEndpoint() {
        return exposeTopologyEndpoint;
    }

    @Override
    public void postInitialize(Fraction.PostInitContext initContext) {
        if (!proxiedServiceMappings.isEmpty()) {
            UndertowFraction undertow = (UndertowFraction) initContext.fraction("undertow");
            HandlerConfiguration handlerConfig = undertow.subresources().handlerConfiguration();
            for (String serviceName : proxiedServiceMappings.keySet()) {
                ReverseProxy proxy = new ReverseProxy(proxyHandlerName(serviceName)).hosts(Collections.emptyList());
                handlerConfig.reverseProxy(proxy);

                String contextPath = proxiedServiceMappings.get(serviceName);
                for (Server server : undertow.subresources().servers()) {
                    Location location = new Location(contextPath).handler(proxyHandlerName(serviceName));
                    for (Host host : server.subresources().hosts()) {
                        host.location(location);
                    }
                }
            }
        }
    }

    private Map<String, String> proxiedServiceMappings = new HashMap<>();

    private boolean exposeTopologyEndpoint = true;

}
