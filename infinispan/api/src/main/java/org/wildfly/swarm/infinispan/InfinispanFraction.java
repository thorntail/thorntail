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

import java.util.Arrays;

/**
 * @author Lance Ball
 */
public class InfinispanFraction extends Infinispan<InfinispanFraction> implements Fraction {

    private InfinispanFraction() {
    }

    public static InfinispanFraction createDefaultFraction() {

        // Default cache
        CacheContainer replicatedCache = new CacheContainer("server")
                .defaultCache("default")
                .aliases(Arrays.asList(new String[]{"singleton", "cluster"}))
                .jgroupsTransport(new JGroupsTransport().lockTimeout(60000L))
                .replicatedCache(new ReplicatedCache("default")
                        .mode("SYNC")
                        .transactionComponent(
                                new TransactionComponent().mode("BATCH")));

        // Web cache
        CacheContainer webCache = new CacheContainer("web")
                .defaultCache("dist")
                .jgroupsTransport(new JGroupsTransport().lockTimeout(60000L))
                .distributedCache(new DistributedCache("dist")
                        .mode("ASYNC")
                        .l1Lifespan(0L)
                        .owners(2)
                        .lockingComponent(new LockingComponent().isolation("REPEATABLE_READ"))
                        .transactionComponent(new TransactionComponent().mode("BATCH"))
                        .fileStore(new FileStore()));

        // EJB cache
        CacheContainer ejbCache = new CacheContainer("ejb")
                .defaultCache("dist")
                .aliases(Arrays.asList(new String[]{"sfsb"}))
                .jgroupsTransport(new JGroupsTransport().lockTimeout(60000L))
                .distributedCache(new DistributedCache("dist")
                        .mode("ASYNC")
                        .l1Lifespan(0l)
                        .owners(2)
                        .lockingComponent(new LockingComponent().isolation("REPEATABLE_READ"))
                        .transactionComponent(new TransactionComponent().mode("BATCH"))
                        .fileStore(new FileStore()));

        // Hibernate cache
        CacheContainer hibernateCache = new CacheContainer("hibernate")
                .defaultCache("local-query")
                .jgroupsTransport(new JGroupsTransport().lockTimeout(60000L))
                .localCache(new LocalCache("local-query")
                        .evictionComponent(new EvictionComponent().maxEntries(10000L).strategy("LRU"))
                        .expirationComponent(new ExpirationComponent().maxIdle(100000L)))
                .invalidationCache(new InvalidationCache("entity")
                        .mode("SYNC")
                        .transactionComponent(new TransactionComponent().mode("NON_XA"))
                        .evictionComponent(new EvictionComponent().maxEntries(10000L).strategy("LRU"))
                        .expirationComponent(new ExpirationComponent().maxIdle(100000L)))
                .replicatedCache(new ReplicatedCache("timestamps").mode("ASYNC"));


        InfinispanFraction fraction = new InfinispanFraction();

        return fraction.cacheContainer(replicatedCache)
                        .cacheContainer(webCache)
                        .cacheContainer(ejbCache)
                        .cacheContainer(hibernateCache);
    }
}
