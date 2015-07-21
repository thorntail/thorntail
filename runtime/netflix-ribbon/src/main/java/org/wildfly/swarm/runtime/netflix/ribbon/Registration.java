package org.wildfly.swarm.runtime.netflix.ribbon;

import org.wildfly.clustering.dispatcher.Command;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author Bob McWhirter
 */
public class Registration implements Command<Void,Object>, Serializable {

    private final String name;
    private final String endpoint;

    public Registration(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;

    }
    @Override
    public Void execute(Object context) throws Exception {
        System.err.println( "got advertisement: " + name + ", " + endpoint );
        return null;
    }
}
