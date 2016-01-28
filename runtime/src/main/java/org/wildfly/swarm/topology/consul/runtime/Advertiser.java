package org.wildfly.swarm.topology.consul.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.container.SocketBinding;
import org.wildfly.swarm.topology.runtime.Registration;

/**
 * Service advertiser providing TTL checks for all registered deployments
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class Advertiser implements Service<Advertiser>, Runnable {

    public static final ServiceName SERVICE_NAME = ConsulService.SERVICE_NAME.append("advertiser");

    private static final Logger log = Logger.getLogger(Advertiser.class.getName());

    private InjectedValue<AgentClient> agentClientInjector = new InjectedValue<>();

    private Set<Registration> advertisements = new HashSet<>();

    private Thread thread;

    public Injector<AgentClient> getAgentClientInjector() {
        return this.agentClientInjector;
    }

    public void advertise(Registration registration) {
        if (this.advertisements.contains(registration)) {
            return;
        }

        AgentClient client = this.agentClientInjector.getValue();
        this.advertisements.add(registration);

        com.orbitz.consul.model.agent.Registration consulReg = ImmutableRegistration.builder()
                .address(registration.getAddress())
                .port(registration.getPort())
                .id(serviceId(registration))
                .name(registration.getName())
                .addTags(registration.getTags())
                .check(com.orbitz.consul.model.agent.Registration.RegCheck.ttl(3L))
                .build();
        client.register(consulReg);
    }

    public void unadvertise(String name, String address, int port) {
        this.advertisements.removeIf( e->e.getName().equals( name)  && e.getAddress().equals( address ) && e.getPort() == port );
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.thread = new Thread(this);

        this.thread.start();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.thread.interrupt();
    }

    @Override
    public Advertiser getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void run() {
        AgentClient client = this.agentClientInjector.getValue();
        while (true) {
            this.advertisements
                    .stream()
                    .forEach(e -> {
                        try {
                            client.pass(serviceId(e));
                        } catch (NotRegisteredException e1) {
                            // ignore?
                            e1.printStackTrace();
                        }
                    });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

    }

    private String serviceId(Registration registration) {
        return registration.getName() + ":" + registration.getAddress() + ":" + registration.getPort();
    }
}
