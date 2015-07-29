package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.loadbalancer.Server;
import org.wildfly.clustering.dispatcher.Command;

/**
 * @author Bob McWhirter
 */
public class UnadvertiseCommand implements Command<Void,ClusterManager> {

    private final String appName;
    private final String nodeKey;

    public UnadvertiseCommand(String nodeKey, String appName) {
        this.nodeKey = nodeKey;
        this.appName = appName;
    }

    @Override
    public Void execute(ClusterManager context) throws Exception {
        context.unregister( this.nodeKey, this.appName );
        return null;
    }
}
