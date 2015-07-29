package org.wildfly.swarm.runtime.netflix.ribbon;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationAdvertiser implements Service<Void> {

    private final String appName;
    private InjectedValue<ClusterManager> clusterManagerInjector = new InjectedValue<ClusterManager>();


    public ApplicationAdvertiser(String appName) {
        this.appName = appName;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.clusterManagerInjector.getValue().advertise( this.appName );
    }

    @Override
    public void stop(StopContext stopContext) {
        this.clusterManagerInjector.getValue().unadvertise( this.appName );
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<ClusterManager> getClusterManagerInjector() {
        return this.clusterManagerInjector;
    }
}
