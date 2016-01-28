package org.wildfly.swarm.topology.consul.runtime;

import java.net.URL;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.container.SocketBindingGroup;
import org.wildfly.swarm.topology.runtime.Registration;
import org.wildfly.swarm.topology.runtime.TopologyConnector;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/**
 * Topology connector for Consul.
 *
 * This topology connector knows how to interact with a Consul catalog to
 * provide topology information to other systems, such as the Topology Web-App
 * and Ribbon.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ConsulTopologyConnector implements Service<ConsulTopologyConnector>, TopologyConnector {

    private final URL url;

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();

    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<>();

    private Advertiser advertiser;

    public ConsulTopologyConnector(URL url) {
        this.url = url;
    }

    public Injector<TopologyManager> getTopologyManagerInjector() {
        return this.topologyManagerInjector;
    }

    public Injector<SocketBinding> getSocketBindingInjector() {
        return this.socketBindingInjector;
    }

    @Override
    public void advertise(String name) {
        SocketBinding binding = this.socketBindingInjector.getValue();

        int portOffset = binding.getSocketBindings().getPortOffset();

        Registration registration = new Registration("consul", name)
                .endPoint(new Registration.EndPoint(
                        binding.getAddress().getHostAddress(),
                        binding.getPort()+portOffset)
                );

        this.advertiser.advertise(registration);
    }

    @Override
    public void unadvertise(String name) {
        this.advertiser.unadvertise(name);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        ServiceTarget target = startContext.getChildTarget();

        ConsulService consul = new ConsulService(this.url);
        target.addService(ConsulService.SERVICE_NAME, consul)
                .install();

        HealthClientService healthClient = new HealthClientService();
        target.addService(HealthClientService.SERIVCE_NAME, healthClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, healthClient.getConsulInjector())
                .install();

        AgentClientService agentClient = new AgentClientService();
        target.addService(AgentClientService.SERVICE_NAME, agentClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, agentClient.getConsulInjector())
                .install();

        CatalogClientService catalogClient = new CatalogClientService();
        target.addService(CatalogClientService.SERVICE_NAME, catalogClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, catalogClient.getConsulInjector())
                .install();


        this.advertiser = new Advertiser();
        target.addService(Advertiser.SERVICE_NAME, advertiser)
                .addDependency(AgentClientService.SERVICE_NAME, AgentClient.class, advertiser.getAgentClientInjector())
                .install();

        CatalogWatcher watcher = new CatalogWatcher();
        target.addService(CatalogWatcher.SERVICE_NAME, watcher)
                .addDependency(CatalogClientService.SERVICE_NAME, CatalogClient.class, watcher.getCatalogClientInjector())
                .addDependency(HealthClientService.SERIVCE_NAME, HealthClient.class, watcher.getHealthClientInjector())
                .addDependency(TopologyManager.SERVICE_NAME, TopologyManager.class, watcher.getTopologyManagerInjector())
                .install();
    }

    @Override
    public void stop(StopContext stopContext) {
        // all sub-services will be stopped prior.
    }

    @Override
    public ConsulTopologyConnector getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }
}
