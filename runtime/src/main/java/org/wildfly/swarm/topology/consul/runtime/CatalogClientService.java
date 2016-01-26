package org.wildfly.swarm.topology.consul.runtime;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * MSC Service providing a Consul CatalogClient
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class CatalogClientService implements Service<CatalogClient> {

    public static final ServiceName SERVICE_NAME = ConsulService.SERVICE_NAME.append("catalog-client");

    private InjectedValue<Consul> consulInjector = new InjectedValue<>();

    private CatalogClient client;

    public Injector<Consul> getConsulInjector() {
        return this.consulInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.client = this.consulInjector.getValue().catalogClient();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.client = null;
    }

    @Override
    public CatalogClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }
}
