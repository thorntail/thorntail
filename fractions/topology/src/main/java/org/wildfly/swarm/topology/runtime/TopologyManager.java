/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.topology.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.AdvertisementHandle;
import org.wildfly.swarm.topology.Topology;
import org.wildfly.swarm.topology.TopologyListener;
import org.wildfly.swarm.topology.TopologyMessages;
import org.wildfly.swarm.topology.deployment.RegistrationAdvertiser;

/**
 * @author Bob McWhirter
 */
public class TopologyManager implements Topology {

    public static final TopologyManager INSTANCE = new TopologyManager();

    public void setServiceTarget(ServiceTarget serviceTarget) {
        this.serviceTarget = serviceTarget;
    }

    public synchronized void addListener(TopologyListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void removeListener(TopologyListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public AdvertisementHandle advertise(String name) {
        ServiceController<Void> httpAdvert = RegistrationAdvertiser.install(this.serviceTarget, name, "http");
        ServiceController<Void> httpsAdvert = RegistrationAdvertiser.install(this.serviceTarget, name, "https");
        return new AdvertisementHandleImpl(httpAdvert, httpsAdvert);
    }

    public synchronized Set<Registration> registrationsForSourceKey(String sourceKey) {
        Set<Registration> result = Collections.unmodifiableSet(
                this.registrations.stream()
                        .filter(e -> e.getSourceKey().equals(sourceKey))
                        .collect(Collectors.toSet())
        );
        return result;
    }

    public synchronized Set<Registration> registrationsForService(String name) {
        Set<Registration> result = Collections.unmodifiableSet(
                this.registrations.stream()
                        .filter(e -> e.getName().equals(name))
                        .collect(Collectors.toSet())
        );

        return result;
    }

    public synchronized Set<Registration> registrationsForService(String name, String tag) {
        Set<Registration> result = Collections.unmodifiableSet(
                this.registrations.stream()
                        .filter(e -> e.getName().equals(name) && e.hasTag(tag))
                        .collect(Collectors.toSet())
        );

        return result;
    }

    public synchronized void register(Registration registration) {
        if (!this.registrations.contains(registration)) {
            this.registrations.add(registration);
            fireListeners();
        }
    }

    public synchronized void unregister(Registration registration) {
        boolean removed = this.registrations.removeIf(e -> e.equals(registration));
        if (removed) {
            fireListeners();
        }
    }

    public synchronized void unregisterAll(String sourceKey) {
        boolean removed = this.registrations.removeIf(e -> e.getSourceKey().equals(sourceKey));
        if (removed) {
            fireListeners();
        }
    }

    public synchronized void unregisterAll(String sourceKey, String name) {
        boolean removed = this.registrations.removeIf(e -> e.getSourceKey().equals(sourceKey) && e.getName().equals(name));
        if (removed) {
            fireListeners();
        }
    }

    @Override
    public synchronized Map<String, List<Entry>> asMap() {
        Map<String, List<Entry>> map = new HashMap<>();

        for (Registration registration : this.registrations) {
            List<Entry> list = map.get(registration.getName());
            if (list == null) {
                list = new ArrayList<>();
                map.put(registration.getName(), list);
            }
            list.add(registration);
        }

        return map;
    }

    private void fireListeners() {
        List<TopologyListener> currentListeners = new ArrayList<>();
        currentListeners.addAll(this.listeners);
        currentListeners.forEach((e) -> {
            executor.execute(() -> {
                try {
                    e.onChange(this);
                } catch (Throwable t) {
                    TopologyMessages.MESSAGES.errorFiringEvent(e.getClass().getName(), t);
                    removeListener(e);
                }
            });
        });
    }

    private List<TopologyListener> listeners = new ArrayList<>();

    private List<Registration> registrations = new ArrayList<>();

    private Executor executor = Executors.newFixedThreadPool(2);

    private ServiceTarget serviceTarget;


}
