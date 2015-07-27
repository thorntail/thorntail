package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.group.Node;

/**
 * @author Bob McWhirter
 */
public class AdvertiseCommand implements Command<Void, ClusterManager> {

    private final String nodeKey;
    private final String appName;
    private final String host;
    private final int port;

    public AdvertiseCommand(String nodeKey, String appName, String host, int port) {
        this.nodeKey = nodeKey;
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    @Override
    public Void execute(ClusterManager context) throws Exception {
        context.register(this.nodeKey, this.appName, new Server(this.host, this.port));
        return null;
    }
}
