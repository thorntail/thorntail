/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.infinispan;

import org.wildfly.swarm.config.Infinispan;
import org.wildfly.swarm.config.infinispan.CacheContainer;
import org.wildfly.swarm.config.infinispan.cache_container.*;
import org.wildfly.swarm.container.Fraction;

import java.io.File;
import java.util.Arrays;

/**
 * @author Lance Ball
 */
public class InfinispanFraction extends Infinispan<InfinispanFraction> implements Fraction {

    private InfinispanFraction() {
    }

    public static InfinispanFraction createDefaultFraction() {

        // Default cache
        CacheContainer<?> replicatedCache = new CacheContainer<>("server")
                .defaultCache("default")
                .alias("singleton")
                .alias("cluster")
                .jgroupsTransport((t) -> {
                    t.lockTimeout(60000L);
                })
                .replicatedCache("default", (c) -> {
                    c.mode("SYNC")
                            .transactionComponent((t) -> {
                                t.mode("BATCH");
                            });
                });

        // Web cache
        CacheContainer<?> webCache = new CacheContainer<>("web")
                .defaultCache("dist")
                .jgroupsTransport((t) -> {
                    t.lockTimeout(60000L);
                })
                .distributedCache("dist", (c) -> {
                    c.mode("ASYNC")
                            .l1Lifespan(0L)
                            .owners(2)
                            .lockingComponent(lc -> {
                                lc.isolation("REPEATABLE_READ");
                            })
                            .transactionComponent(tc -> {
                                tc.mode("BATCH");
                            })
                            .fileStore();
                });


        // EJB cache
        CacheContainer<?> ejbCache = new CacheContainer<>("ejb")
                .defaultCache("dist")
                .alias("sfsb")
                .jgroupsTransport(t -> {
                    t.lockTimeout(60000L);
                })
                .distributedCache("dist", (c) -> {
                    c.mode("ASYNC")
                            .l1Lifespan(0l)
                            .owners(2)
                            .lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                            .transactionComponent(t -> t.mode("BATCH"))
                            .fileStore();
                });

        // Hibernate cache
        CacheContainer<?> hibernateCache = new CacheContainer<>("hibernate")
                .defaultCache("local-query")
                .jgroupsTransport(t -> {
                    t.lockTimeout(60000L);
                })
                .localCache("local-query", (c) -> {
                    c.evictionComponent(ec ->
                            ec.maxEntries(10000L).strategy("LRU")
                    );
                    c.expirationComponent(ec ->
                            ec.maxIdle(100000L)
                    );
                })
                .invalidationCache("entity", (c) -> {
                    c.mode("SYNC")
                            .transactionComponent(tc -> tc.mode("NON_XA"))
                            .evictionComponent(ec -> ec.maxEntries(10000L).strategy("LRU"))
                            .expirationComponent(ec -> ec.maxIdle(100000L));
                })
                .replicatedCache("timestamps", (c) -> {
                    c.mode("ASYNC");
                });


        InfinispanFraction fraction = new InfinispanFraction();

        return fraction.cacheContainer(replicatedCache)
                .cacheContainer(webCache)
                .cacheContainer(ejbCache)
                .cacheContainer(hibernateCache);
    }
}
