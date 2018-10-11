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
package org.wildfly.swarm.topology.webapp;

import java.util.HashMap;
import java.util.Map;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;

import static org.wildfly.swarm.spi.api.Defaultable.*;

/**
 * @author Lance Ball
 */
@Configurable("thorntail.topology.web-app")
public class TopologyWebAppFraction implements Fraction<TopologyWebAppFraction> {

    public static final String DEFAULT_CONTEXT = "/topology";

    public TopologyWebAppFraction() {
    }

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
        this.exposeTopologyEndpoint.set(exposeTopologyEndpoint);
    }

    public boolean exposeTopologyEndpoint() {
        return exposeTopologyEndpoint.get();
    }

    @AttributeDocumentation("Service name to URL path proxy mappings")
    private Map<String, String> proxiedServiceMappings = new HashMap<>();

    @AttributeDocumentation("Flag to enable or disable the topology web endpoint")
    private Defaultable<Boolean> exposeTopologyEndpoint = bool(true);

}
