package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.wildfly.swarm.netflix.ribbon.RibbonTopology;
import org.wildfly.swarm.netflix.ribbon.RibbonTopologyListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class ClusterRegistry implements RibbonTopology {

    public static final ClusterRegistry INSTANCE = new ClusterRegistry();

    private List<Registration> registrations = new ArrayList<>();

    private List<RibbonTopologyListener> listeners = new ArrayList<>();

    public void addListener(RibbonTopologyListener listener) {
        this.listeners.add( listener );
    }

    public void removeListener(RibbonTopologyListener listener) {
        this.listeners.remove( listener );
    }

    public synchronized List<Server> getServers(String appName) {
        return this.registrations
                .stream()
                .filter((e) -> e.appName.equals(appName))
                .map((e) -> e.server)
                .collect(Collectors.toList());
    }

    public synchronized void register(String nodeKey, String appName, Server server) {
        if ( ! hasRegistration( nodeKey, appName, server ) ) {
            this.registrations.add(new Registration(nodeKey, appName, server));
            fireListeners();
        }
    }

    protected synchronized long countRegistrations(String nodeKey, String appName, Server server) {
        return this.registrations.stream()
                .filter((e) -> e.nodeKey.equals(nodeKey) && e.appName.equals(appName))
                .collect(Collectors.counting());
    }

    protected synchronized boolean hasRegistration(String nodeKey, String appName, Server server) {
        return this.registrations.stream().anyMatch((e) -> e.nodeKey.equals(nodeKey) && e.appName.equals(appName));
    }

    public synchronized void unregister(String nodeKey, String appName) {
        boolean removed = this.registrations.removeIf((e) -> e.nodeKey.equals(nodeKey) && e.appName.equals(appName));
        if ( removed ) {
            fireListeners();
        }
    }

    public synchronized void unregisterAll(String nodeKey) {
        boolean removed = this.registrations.removeIf((e) -> e.nodeKey.equals(nodeKey));
        if ( removed ) {
            fireListeners();
        }
    }

    private void fireListeners() {
        this.listeners.forEach( (e)->{
            e.onChange(this);
        });
    }

    @Override
    public synchronized Map<String,List<String>> asMap() {
        Map<String,List<String>> map = new HashMap<>();

        this.registrations.forEach( (reg)->{
            List<String> list = map.get(reg.appName);
            if ( list == null ) {
                list = new ArrayList<String>();
                map.put( reg.appName, list );
            }

            list.add( reg.server.toString() );
        });

        return map;
    }

    private static class Registration {
        public String nodeKey;
        public String appName;
        public Server server;

        public Registration(String nodeKey, String appName, Server server) {
            this.nodeKey = nodeKey;
            this.appName = appName;
            this.server = server;
        }
    }

}
