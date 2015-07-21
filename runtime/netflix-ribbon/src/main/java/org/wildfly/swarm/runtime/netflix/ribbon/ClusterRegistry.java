package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;

import java.util.*;

/**
 * @author Bob McWhirter
 */
public class ClusterRegistry {

    public static final ClusterRegistry INSTANCE = new ClusterRegistry();

    private Map<String,List<Server>> servers = new HashMap<>();

    public synchronized List<Server> getServers(String appName) {
        if ( servers.containsKey( appName ) ) {
            return servers.get( appName );
        }

        return Collections.emptyList();
    }

    public synchronized void register(String appName, Server server) {
        System.err.println( "register: " + appName + " // " + server );
        List<Server> list = this.servers.get(appName);
        if ( list == null ) {
            list = new ArrayList<>();
            this.servers.put( appName, list );
        }

        list.add( server );
    }

}
