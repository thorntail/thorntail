package org.wildfly.swarm.topology.runtime;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class RegistrationAdvertiser implements Service<Void> {

    private final String name;
    private final String[] tags;

    private InjectedValue<TopologyConnector> topologyConnectorInjector = new InjectedValue<>();
    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<>();

    public RegistrationAdvertiser(String name, String...tags) {
        this.name = name;
        this.tags = tags;
    }

    public Injector<TopologyConnector> getTopologyConnectorInjector() {
        return this.topologyConnectorInjector;
    }

    public Injector<SocketBinding> getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.topologyConnectorInjector.getValue().advertise(this.name, this.socketBindingInjector.getValue(), this.tags );
    }

    @Override
    public void stop(StopContext stopContext) {
        this.topologyConnectorInjector.getValue().unadvertise(this.name, this.socketBindingInjector.getValue() );
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
