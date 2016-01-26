package org.wildfly.swarm.topology.runtime;

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

    private InjectedValue<TopologyConnector> topologyConnectorInjector = new InjectedValue<>();

    public RegistrationAdvertiser(String name) {
        this.name = name;
    }

    public Injector<TopologyConnector> getTopologyConnectorInjector() {
        return this.topologyConnectorInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.topologyConnectorInjector.getValue().advertise(this.name);
    }

    @Override
    public void stop(StopContext stopContext) {
        this.topologyConnectorInjector.getValue().unadvertise(this.name);
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
