package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.wildfly.clustering.dispatcher.Command;

/**
 * @author Bob McWhirter
 */
public class AdvertiseCommand implements Command<Void,ClusterRegistry> {

    private final String appName;
    private final String host;
    private final int port;

    public AdvertiseCommand(String appName, String host, int port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    @Override
    public Void execute(ClusterRegistry context) throws Exception {
        System.err.println( "running advertise command with " + this.appName + ", " + this.host + ", " + this.port + ", against " + context );
        try {
            context.register(this.appName, new Server(this.host, this.port));
        } catch ( Throwable t ) {
            t.printStackTrace();
        }
        return null;
    }
}
