package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class ClusterRegistry {

    public static final ClusterRegistry INSTANCE = new ClusterRegistry();

    private List<Registration> registrations = new ArrayList<>();


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
        }
    }

    protected synchronized long countRegistrations(String nodeKey, String appName, Server server) {
        return this.registrations.stream()
                .filter((e) -> e.nodeKey.equals(nodeKey) && e.appName.equals(appName))
                .collect(Collectors.counting());
    }

    protected synchronized boolean hasRegistration(String nodeKey, String appName, Server server) {
        return this.registrations.stream().anyMatch( (e)->e.nodeKey.equals( nodeKey ) && e.appName.equals(appName) );
    }

    public synchronized void unregister(String nodeKey, String appName) {
        this.registrations.removeIf((e) -> e.nodeKey.equals(nodeKey) && e.appName.equals(appName));
    }

    public synchronized void unregisterAll(String nodeKey) {
        this.registrations.removeIf((e) -> e.nodeKey.equals(nodeKey));
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
