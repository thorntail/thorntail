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
package org.wildfly.swarm.topology.consul.runtime;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.orbitz.consul.cache.ConsulCache.Listener;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;
import org.wildfly.swarm.topology.runtime.TopologyManager;
import org.wildfly.swarm.topology.runtime.Registration;

/**
 * Service-cache listener.
 *
 * This cache listener is responsible for receiving notifications
 * of cache changes and calculating the differences to apply to the
 * underlying TopologyManager.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ServiceCacheListener implements Listener<ServiceHealthKey, ServiceHealth> {

    public ServiceCacheListener(String name, TopologyManager topologyManager) {
        this.name = name;
        this.topologyManager = topologyManager;
    }

    @Override
    public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
        Set<Registration> previousEntries = topologyManager.registrationsForService(this.name);

        Set<Registration> newEntries = newValues.values().stream()
                .map(e -> new Registration("consul",
                                           this.name,
                                           e.getService().getAddress(),
                                           e.getService().getPort())
                        .addTags(e.getService().getTags())
                )
                .collect(Collectors.toSet());

        previousEntries.stream()
                .filter(e -> !newEntries.contains(e))
                .forEach(e -> {
                    this.topologyManager.unregister(e);
                });

        newEntries.stream()
                .filter(e -> !previousEntries.contains(e))
                .forEach(e -> {
                    this.topologyManager.register(e);
                });
    }

    private final String name;

    private final TopologyManager topologyManager;
}
