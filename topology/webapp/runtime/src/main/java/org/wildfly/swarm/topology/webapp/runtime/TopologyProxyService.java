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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.undertow.deployment.GlobalRequestControllerHandler;
import org.wildfly.swarm.topology.Topology;
import org.wildfly.swarm.topology.TopologyListener;
import org.wildfly.swarm.topology.webapp.TopologyWebAppFraction;

public class TopologyProxyService implements Service<TopologyProxyService>, TopologyListener {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm.topology.proxy");

    public TopologyProxyService(Set<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public ServiceName mscServiceNameForServiceProxy(String serviceName) {
        return ServiceName.of("jboss", "undertow", "handler",
                TopologyWebAppFraction.proxyHandlerName(serviceName));
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            Topology topology = Topology.lookup();
            topology.addListener(this);
        } catch (NamingException ex) {
            throw new StartException(ex);
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public TopologyProxyService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void onChange(Topology topology) {
        Map<String, List<Topology.Entry>> topologyMap = topology.asMap();
        for (String serviceName : serviceNames) {
            if (topologyMap.containsKey(serviceName)) {
                updateProxyHosts(serviceName, topologyMap.get(serviceName));
            } else {
                // All instances of this service went away
                updateProxyHosts(serviceName, Collections.emptyList());
            }
        }
    }

    public Injector<HttpHandler> getHandlerInjectorFor(String serviceName) {
        InjectedValue<HttpHandler> injector = proxyHandlerMap.get(serviceName);
        if (injector == null) {
            injector = new InjectedValue<>();
            proxyHandlerMap.put(serviceName, injector);
        }
        return injector;
    }

    private void updateProxyHosts(String serviceName, List<Topology.Entry> entries) {
        HttpHandler proxyHandler = proxyHandlerMap.get(serviceName).getValue();
        LoadBalancingProxyClient proxyClient = null;

        // with SWARM-189 the request controller subsystem does replace
        // all HttpHandler (including ProxyHandler) with GlobalRequestControllerHandler,
        // which then wraps the next handler in the chain
        if(proxyHandler instanceof GlobalRequestControllerHandler) {
            ProxyHandler proxy = (ProxyHandler)((GlobalRequestControllerHandler)proxyHandler).getNext(); // next in the chain of handlers
            proxyClient = (LoadBalancingProxyClient) proxy.getProxyClient();
        }
        else {
            proxyClient = (LoadBalancingProxyClient) ((ProxyHandler) proxyHandler).getProxyClient();
        }

        List<Topology.Entry> oldEntries = proxyEntries.get(serviceName);
        List<Topology.Entry> entriesToRemove = new ArrayList<>();
        List<Topology.Entry> entriesToAdd = new ArrayList<>();
        if (oldEntries == null) {
            entriesToAdd.addAll(entries);
        } else {
            for (Topology.Entry oldEntry : oldEntries) {
                if (!entries.contains(oldEntry)) {
                    entriesToRemove.add(oldEntry);
                }
            }
            for (Topology.Entry entry : entries) {
                if (!oldEntries.contains(entry)) {
                    entriesToAdd.add(entry);
                }
            }
        }
        for (Topology.Entry entry : entriesToRemove) {
            try {
                proxyClient.removeHost(entryToURI(entry));
            } catch (URISyntaxException ex) {
                log.log(Level.WARNING, "Error converting topology entry to URI", ex);
            }
        }
        for (Topology.Entry entry : entriesToAdd) {
            try {
                proxyClient.addHost(entryToURI(entry));
            } catch (URISyntaxException ex) {
                log.log(Level.WARNING, "Error converting topology entry to URI", ex);
            }
        }
        proxyEntries.put(serviceName, entries);
    }

    private URI entryToURI(Topology.Entry entry) throws URISyntaxException {
        List<String> tags = entry.getTags();
        String scheme = "http";
        if (tags.contains("https")) {
            scheme = "https";
        }
        return new URI(scheme, null, entry.getAddress(), entry.getPort(), null, null, null);
    }

    private static final Logger log = Logger.getLogger(TopologyProxyService.class.getName());

    private final Set<String> serviceNames;

    private Map<String, InjectedValue<HttpHandler>> proxyHandlerMap = new HashMap<>();

    private Map<String, List<Topology.Entry>> proxyEntries = new HashMap<>();
}
