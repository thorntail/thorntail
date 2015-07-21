package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.wildfly.clustering.dispatcher.Command;

/**
 * @author Bob McWhirter
 */
public class UnadvertiseCommand implements Command<Void,ClusterRegistry> {

    private final String appName;
    private final String host;
    private final int port;

    public UnadvertiseCommand(String appName, String host, int port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    @Override
    public Void execute(ClusterRegistry context) throws Exception {
        //context.register( this.appName, new Server( this.host, this.port ));
        System.err.println( "Unadvertise" );
        return null;
    }
}
