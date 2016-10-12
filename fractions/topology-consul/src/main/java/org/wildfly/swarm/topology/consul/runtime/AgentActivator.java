package org.wildfly.swarm.topology.consul.runtime;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.consul.ConsulTopologyFraction;
import org.wildfly.swarm.topology.consul.runtime.Advertiser;
import org.wildfly.swarm.topology.consul.runtime.AgentClientService;
import org.wildfly.swarm.topology.consul.runtime.CatalogClientService;
import org.wildfly.swarm.topology.consul.runtime.ConsulService;
import org.wildfly.swarm.topology.consul.runtime.HealthClientService;

/**
 * @author Heiko Braun
 * @since 18/05/16
 */
@Singleton
public class AgentActivator implements ServiceActivator {


    @Inject
    @Any
    ConsulTopologyFraction fraction;


    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        ServiceTarget target = context.getServiceTarget();

        ConsulService consul = new ConsulService(this.fraction.url());
        target.addService(ConsulService.SERVICE_NAME, consul)
                .install();

        HealthClientService healthClient = new HealthClientService();
        target.addService(HealthClientService.SERIVCE_NAME, healthClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, healthClient.getConsulInjector())
                .install();


        CatalogClientService catalogClient = new CatalogClientService();
        target.addService(CatalogClientService.SERVICE_NAME, catalogClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, catalogClient.getConsulInjector())
                .install();

        AgentClientService agentClient = new AgentClientService();
        target.addService(AgentClientService.SERVICE_NAME, agentClient)
                .addDependency(ConsulService.SERVICE_NAME, Consul.class, agentClient.getConsulInjector())
                .install();

        Advertiser advertiser = new Advertiser();
        target.addService(Advertiser.SERVICE_NAME, advertiser)
                .addDependency(AgentClientService.SERVICE_NAME, AgentClient.class, advertiser.getAgentClientInjector())
                .install();

    }
}
