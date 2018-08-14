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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.swarm.topology.consul.ConsulTopologyMessages;
import org.wildfly.swarm.topology.runtime.TopologyManager;

/**
 * Catalog-watching service.
 *
 * This service regularly performs a blocking-wait poll of the catalog of all
 * services in order to avoid having to know a-priori which services are of
 * interest to the application.
 *
 * Any discovered service is setup with a related health-cache listener in
 * order to maintain the full topology.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class CatalogWatcher implements Service<CatalogWatcher>, Runnable {

    public static final ServiceName SERVICE_NAME = ConsulService.SERVICE_NAME.append("catalog-watcher");

    public Injector<CatalogClient> getCatalogClientInjector() {
        return this.catalogClientInjector;
    }

    public Injector<HealthClient> getHealthClientInjector() {
        return this.healthClientInjector;
    }

    public Injector<TopologyManager> getTopologyManagerInjector() {
        return this.topologyManagerInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.thread.interrupt();

        this.watchers.entrySet().forEach(e -> {
            try {
                e.getValue().stop();
            } catch (Exception ex) {
                ConsulTopologyMessages.MESSAGES.errorStoppingCatalogWatcher(e.getKey(), ex);
            }
        });
    }

    @Override
    public CatalogWatcher getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    @Override
    public void run() {
        CatalogClient client = this.catalogClientInjector.getValue();

        BigInteger index = null;

        while (true) {

            try {
                QueryOptions options = QueryOptions.BLANK;

                if (index != null) {
                    options = ImmutableQueryOptions.builder()
                            .wait("60s")
                            .index(index)
                            .build();
                }

                ConsulResponse<Map<String, List<String>>> services = client.getServices(options);

                index = services.getIndex();

                Map<String, List<String>> response = services.getResponse();

                response.keySet().forEach(e -> {
                    setupWatcher(e);
                });
            } catch (Exception ex) {
                ConsulTopologyMessages.MESSAGES.errorOnCatalogUpdate(ex);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void setupWatcher(String serviceName) {
        if (watchers.containsKey(serviceName)) {
            return;
        }
        QueryOptions options = ImmutableQueryOptions.builder()
                .build();

        ServiceHealthCache healthCache = ServiceHealthCache.newCache(
                this.healthClientInjector.getValue(),
                serviceName,
                true,
                options,
                5
        );

        try {
            healthCache.addListener(new ServiceCacheListener(serviceName, this.topologyManagerInjector.getValue()));
            healthCache.start();
            healthCache.awaitInitialized(1, TimeUnit.SECONDS);
            this.watchers.put(serviceName, healthCache);
        } catch (Exception e) {
            ConsulTopologyMessages.MESSAGES.errorSettingUpCatalogWatcher(serviceName, e);
        }
    }

    private InjectedValue<CatalogClient> catalogClientInjector = new InjectedValue<>();

    private InjectedValue<HealthClient> healthClientInjector = new InjectedValue<>();

    private InjectedValue<TopologyManager> topologyManagerInjector = new InjectedValue<>();

    private Thread thread;

    private Map<String, ServiceHealthCache> watchers = new HashMap<>();
}
