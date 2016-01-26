package org.wildfly.swarm.topology.consul.runtime;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * MSC service for the Consul HealthClient.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class HealthClientService implements Service<HealthClient> {

    public static final ServiceName SERIVCE_NAME = ConsulService.SERVICE_NAME.append("health-client");

    private InjectedValue<Consul> consulInjector = new InjectedValue<>();

    private HealthClient healthClient;

    public Injector<Consul> getConsulInjector() {
        return this.consulInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.healthClient = this.consulInjector.getValue().healthClient();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.healthClient = null;
    }

    @Override
    public HealthClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.healthClient;
    }
}
