package org.wildfly.swarm.topology.webapp.runtime;

import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.Topology;
import org.wildfly.swarm.topology.TopologyListener;
import org.wildfly.swarm.topology.webapp.TopologyWebAppFraction;

import javax.naming.NamingException;
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

public class TopologyProxyService implements Service<TopologyProxyService>, TopologyListener {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm.topology.proxy");
    private static final Logger log = Logger.getLogger(TopologyProxyService.class.getName());

    private final Set<String> serviceNames;
    private Map<String, InjectedValue<ProxyHandler>> proxyHandlerMap = new HashMap<>();
    private Map<String, List<Topology.Entry>> proxyEntries = new HashMap<>();

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

    public Injector<ProxyHandler> getHandlerInjectorFor(String serviceName) {
        InjectedValue<ProxyHandler> injector = proxyHandlerMap.get(serviceName);
        if (injector == null) {
            injector = new InjectedValue<>();
            proxyHandlerMap.put(serviceName, injector);
        }
        return injector;
    }

    private void updateProxyHosts(String serviceName, List<Topology.Entry> entries) {
        ProxyHandler proxyHandler = proxyHandlerMap.get(serviceName).getValue();
        LoadBalancingProxyClient proxyClient = (LoadBalancingProxyClient) proxyHandler.getProxyClient();
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
}
