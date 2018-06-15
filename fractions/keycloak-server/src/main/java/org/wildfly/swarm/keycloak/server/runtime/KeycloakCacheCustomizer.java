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
package org.wildfly.swarm.keycloak.server.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.config.infinispan.CacheContainer;
import org.wildfly.swarm.config.infinispan.Mode;
import org.wildfly.swarm.config.infinispan.cache_container.EvictionComponent;
import org.wildfly.swarm.infinispan.InfinispanFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class KeycloakCacheCustomizer implements Customizer {

    @Inject
    @Any
    private InfinispanFraction infinispan;


    @Override
    public void customize() {

        CacheContainer cache = infinispan.subresources().cacheContainer("keycloak");
        if (cache == null) {
            infinispan.cacheContainer("keycloak", (c) -> c.jndiName("infinispan/Keycloak")
                    .localCache("realms", (localCache) -> {
                        localCache.evictionComponent((evict) -> {
                            evict.maxEntries(10000L);
                            evict.strategy(EvictionComponent.Strategy.LRU);
                        });
                    })
                    .localCache("users", (localCache) -> {
                        localCache.evictionComponent((evict) -> {
                            evict.maxEntries(10000L);
                            evict.strategy(EvictionComponent.Strategy.LRU);
                        });
                    })
                    .localCache("sessions")
                    .localCache("authenticationSessions")
                    .localCache("offlineSessions")
                    .localCache("loginFailures")
                    .localCache("work")
                    .localCache("authorization", (localCache) -> {
                        localCache.evictionComponent((evict) -> {
                            evict.strategy(EvictionComponent.Strategy.LRU);
                            evict.maxEntries(100L);

                        });
                    })
                    .localCache("keys", (localCache) -> {
                        localCache.evictionComponent((evict) -> {
                            evict.strategy(EvictionComponent.Strategy.LRU);
                            evict.maxEntries(1000L);
                        });
                        localCache.expirationComponent((expire) -> {
                            expire.maxIdle(3600000L);
                        });
                    })
                    .localCache("actionTokens", (localCache) -> {
                        localCache.evictionComponent((evict) -> {
                            evict.strategy(EvictionComponent.Strategy.NONE);
                            evict.maxEntries((long) -1);
                        });
                        localCache.expirationComponent((expire) -> {
                            expire.maxIdle((long) -1);
                            expire.interval((long) 300000);
                        });
                    })
                    .distributedCache("clientSessions", (dCache) -> {
                        dCache.mode(Mode.SYNC).owners(1);
                    })
                    .distributedCache("offlineClientSessions", (dCache) -> {
                        dCache.mode(Mode.SYNC).owners(1);
                    })
            );
        }

    }
}
