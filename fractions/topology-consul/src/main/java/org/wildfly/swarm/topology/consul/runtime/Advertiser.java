/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.topology.consul.runtime;

import java.util.Collections;
import java.util.Set;
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
import org.wildfly.swarm.topology.TopologyMessages;
import org.wildfly.swarm.topology.runtime.Registration;

/**
 * Service advertiser providing TTL checks for all registered deployments
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class Advertiser implements Service<Advertiser>, Runnable {

    public static final ServiceName SERVICE_NAME = ConsulService.SERVICE_NAME.append("advertiser");

    public Injector<AgentClient> getAgentClientInjector() {
        return this.agentClientInjector;
    }

    public void advertise(Registration registration) {
        if (this.advertisements.contains(registration)) {
            return;
        }

        AgentClient client = this.agentClientInjector.getValue();

        com.orbitz.consul.model.agent.Registration consulReg = ImmutableRegistration.builder()
                .address(registration.getAddress())
                .port(registration.getPort())
                .id(serviceId(registration))
                .name(registration.getName())
                .addTags(registration.getTags().toArray(new String[]{}))
                .check(com.orbitz.consul.model.agent.Registration.RegCheck.ttl(checkTTL))
                .build();
        client.register(consulReg);

        this.advertisements.add(registration);

        log.info("Registered service " + consulReg.getId() + ", checkTTL: " + checkTTL);
    }

    public void unadvertise(String name, String address, int port) {

        AgentClient client = this.agentClientInjector.getValue();
        Registration r = new Registration("consul", name, address, port, "");

        this.advertisements
                .stream()
                .filter(e -> e.equals(r))
                .forEach(e -> {
                    String serviceId = serviceId(e);
                    log.info("Deregister service " + serviceId);
                    client.deregister(serviceId);
                });
        this.advertisements.removeIf(e -> e.equals(r));
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
                        } catch (NotRegisteredException ex) {
                            TopologyMessages.MESSAGES.notRegistered(e.toString(), ex);
                        } catch (Exception ex) {
                            TopologyMessages.MESSAGES.errorOnCheck(e.toString(), ex);
                        }
                    });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // ignore
                break;
            }
        }

    }

    public long getCheckTTL() {
        return checkTTL;
    }

    public void setCheckTTL(long checkTTL) {
        this.checkTTL = checkTTL;
    }

    private String serviceId(Registration registration) {
        return registration.getName() + ":" + registration.getAddress() + ":" + registration.getPort();
    }

    private static final Logger log = Logger.getLogger(Advertiser.class.getName());

    private InjectedValue<AgentClient> agentClientInjector = new InjectedValue<>();

    private Set<Registration> advertisements = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Thread thread;

    private long checkTTL = 3L;
}
