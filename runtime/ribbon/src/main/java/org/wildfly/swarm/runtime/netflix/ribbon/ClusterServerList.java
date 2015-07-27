package org.wildfly.swarm.runtime.netflix.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ClusterServerList extends AbstractServerList<Server> {

    private String appName;

    @Override
    public void initWithNiwsConfig(IClientConfig config) {
        this.appName = config.getClientName();
    }

    @Override
    public List<Server> getInitialListOfServers() {
        return ClusterRegistry.INSTANCE.getServers( this.appName );
    }

    @Override
    public List<Server> getUpdatedListOfServers() {
        return ClusterRegistry.INSTANCE.getServers( this.appName );
    }
}
