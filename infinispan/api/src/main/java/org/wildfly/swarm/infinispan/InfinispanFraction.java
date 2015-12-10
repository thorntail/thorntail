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
import org.wildfly.swarm.config.infinispan.cache_container.DistributedCache;
import org.wildfly.swarm.config.infinispan.cache_container.LocalCache;
import org.wildfly.swarm.config.infinispan.cache_container.ReplicatedCache;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;

import java.util.concurrent.TimeUnit;

/**
 * @author Lance Ball
 * @author Toby Crawley
 */
public class InfinispanFraction extends Infinispan<InfinispanFraction> implements Fraction {

    private InfinispanFraction() {
    }

    public static InfinispanFraction createDefaultFraction() {
        return new InfinispanFraction().markDefaultFraction();
    }

    @Override
    public void postInitialize(Container.PostInitContext initContext) {
        if (this.defaultFraction) {
            if (initContext.hasFraction("jgroups")) {
                clusteredDefaultFraction();
            } else {
                localDefaultFraction();
            }
        }
    }

    public void applyResourceDefaults() {
        subresources().cacheContainers().forEach(cc -> {
            enableResourceDefaults(cc);
            cc.subresources().distributedCaches().forEach(InfinispanFraction::enableResourceDefaults);
            cc.subresources().localCaches().forEach(InfinispanFraction::enableResourceDefaults);
            cc.subresources().replicatedCaches().forEach(InfinispanFraction::enableResourceDefaults);
        });
    }

    protected InfinispanFraction markDefaultFraction() {
        this.defaultFraction = true;

        return this;
    }

    private InfinispanFraction clusteredDefaultFraction() {
        cacheContainer("server",
                       cc -> cc.defaultCache("default")
                               .alias("singleton")
                               .alias("cluster")
                               .jgroupsTransport(t -> t.lockTimeout(60000L))
                               .replicatedCache("default",
                                                c -> c.mode("SYNC")
                                                        .transactionComponent(t -> t.mode("BATCH"))));

        cacheContainer("web",
                       cc -> cc.defaultCache("dist")
                               .jgroupsTransport(t -> t.lockTimeout(60000L))
                               .distributedCache("dist",
                                                 c -> c.mode("ASYNC")
                                                         .l1Lifespan(0L)
                                                         .owners(2)
                                                         .lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                         .transactionComponent(tc -> tc.mode("BATCH"))
                                                         .fileStore()));

        cacheContainer("ejb",
                       cc -> cc.defaultCache("dist")
                               .alias("sfsb")
                               .jgroupsTransport(t -> t.lockTimeout(60000L))
                               .distributedCache("dist",
                                                 c -> c.mode("ASYNC")
                                                         .l1Lifespan(0L)
                                                         .owners(2)
                                                         .lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                         .transactionComponent(t -> t.mode("BATCH"))
                                                         .fileStore()));

        cacheContainer("hibernate",
                       cc -> cc.defaultCache("local-query")
                               .jgroupsTransport(t -> t.lockTimeout(60000L))
                               .localCache("local-query",
                                           c -> c.evictionComponent(ec -> ec.maxEntries(10000L).strategy("LRU"))
                                                   .expirationComponent(ec -> ec.maxIdle(100000L)))
                               .invalidationCache("entity",
                                                  c -> c.mode("SYNC")
                                                          .transactionComponent(tc -> tc.mode("NON_XA"))
                                                          .evictionComponent(ec -> ec.maxEntries(10000L).strategy("LRU"))
                                                          .expirationComponent(ec -> ec.maxIdle(100000L)))
                               .replicatedCache("timestamps", c -> c.mode("ASYNC")));

        return this;
    }

    private InfinispanFraction localDefaultFraction() {
        cacheContainer("server",
                       cc -> cc.defaultCache("default")
                               .localCache("default", c -> c.transactionComponent(t -> t.mode("BATCH")))
                               .remoteCommandThreadPool());

        cacheContainer("web",
                       cc -> cc.defaultCache("passivation")
                               .localCache("passivation",
                                           c -> c.lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                   .transactionComponent(tc -> tc.mode("BATCH"))
                                                   .fileStore(fs -> fs.passivation(true).purge(false)))
                               .localCache("persistent",
                                           c -> c.lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                   .transactionComponent(tc -> tc.mode("BATCH"))
                                                   .fileStore(fs -> fs.passivation(false).purge(false))));

        cacheContainer("ejb",
                       cc -> cc.alias("sfsb")
                               .defaultCache("passivation")
                               .localCache("passivation",
                                           c -> c.lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                   .transactionComponent(tc -> tc.mode("BATCH"))
                                                   .fileStore(fs -> fs.passivation(true).purge(false)))
                               .localCache("persistent",
                                           c -> c.lockingComponent(lc -> lc.isolation("REPEATABLE_READ"))
                                                   .transactionComponent(tc -> tc.mode("BATCH"))
                                                   .fileStore(fs -> fs.passivation(false).purge(false))));

        cacheContainer("hibernate",
                       cc -> cc.defaultCache("local-query")
                               .localCache("entity",
                                           c -> c.transactionComponent(t -> t.mode("NON_XA"))
                                                   .evictionComponent(e -> e.strategy("LRU").maxEntries(10000L))
                                                   .expirationComponent(e -> e.maxIdle(100000L)))
                               .localCache("immutable-entity",
                                           c -> c.transactionComponent(t -> t.mode("NON_XA"))
                                                   .evictionComponent(e -> e.strategy("LRU").maxEntries(10000L))
                                                   .expirationComponent(e -> e.maxIdle(100000L)))
                               .localCache("local-query",
                                           c -> c.transactionComponent(t -> t.mode("NON_XA"))
                                                   .evictionComponent(e -> e.strategy("LRU").maxEntries(10000L))
                                                   .expirationComponent(e -> e.maxIdle(100000L)))
                               .localCache("timestamps"));

        return this;
    }

    
    // applies defaults to the cache-container, since infinispan applies these defaults at
    // xml read time, instead of at model add time
    // https://issues.jboss.org/browse/WFLY-5672
    private static CacheContainer enableResourceDefaults(CacheContainer container) {
        CacheContainer.CacheContainerResources containerResources = container.subresources();

        // from https://github.com/wildfly/wildfly/tree/10.0.0.CR4/clustering/infinispan/extension/src/main/java/org/jboss/as/clustering/infinispan/subsystem/ThreadPoolResourceDefinition.java#L71
        if (containerResources.asyncOperationsThreadPool() == null) {
            container.asyncOperationsThreadPool(p -> p.minThreads(25)
                    .maxThreads(25)
                    .queueLength(1000)
                    .keepaliveTime(60000L));
        }
        if (containerResources.listenerThreadPool() == null) {
            container.listenerThreadPool(p -> p.minThreads(1)
                    .maxThreads(1)
                    .queueLength(100000)
                    .keepaliveTime(60000L));
        }
        if (containerResources.persistenceThreadPool() == null) {
            container.persistenceThreadPool(p -> p.minThreads(1)
                    .maxThreads(4)
                    .queueLength(0)
                    .keepaliveTime(60000L));
        }
        if (containerResources.remoteCommandThreadPool() == null) {
            container.remoteCommandThreadPool(p -> p.minThreads(1)
                    .maxThreads(200)
                    .queueLength(0)
                    .keepaliveTime(60000L));
        }
        if (containerResources.stateTransferThreadPool() == null) {
            container.stateTransferThreadPool(p -> p.minThreads(1)
                    .maxThreads(60)
                    .queueLength(0)
                    .keepaliveTime(60000L));
        }
        if (containerResources.transportThreadPool() == null) {
            container.transportThreadPool(p -> p.minThreads(25)
                    .maxThreads(25)
                    .queueLength(100000)
                    .keepaliveTime(60000L));
        }

        // from https://github.com/wildfly/wildfly/tree/10.0.0.CR4/clustering/infinispan/extension/src/main/java/org/jboss/as/clustering/infinispan/subsystem/ScheduledThreadPoolResourceDefinition.java#L72
        if (containerResources.expirationThreadPool() == null) {
            container.expirationThreadPool(p -> p.maxThreads(1)
                    .keepaliveTime(60000L));
        }

        if (containerResources.jgroupsTransport() == null &&
                containerResources.noneTransport() == null) {
            container.noneTransport();
        }


        return container;
    }

    private static LocalCache enableResourceDefaults(LocalCache cache) {
        // from https://github.com/wildfly/wildfly/tree/10.0.0.CR4/clustering/infinispan/extension/src/main/java/org/jboss/as/clustering/infinispan/subsystem/ScheduledThreadPoolResourceDefinition.java#L346-L361
        LocalCache.LocalCacheResources cacheResources = cache.subresources();
        if (cacheResources.evictionComponent() == null) {
            cache.evictionComponent(c -> c.maxEntries(-1L)
                    .strategy("NONE"));
        }

        if (cacheResources.expirationComponent() == null) {
            cache.expirationComponent(c -> c.interval(60000L)
                    .lifespan(-1L)
                    .maxIdle(-1L));
        }

        if (cacheResources.lockingComponent() == null) {
            cache.lockingComponent(c -> c.acquireTimeout(15000L)
                    .concurrencyLevel(1000)
                    .isolation("READ_COMMITTED")
                    .striping(false));
        }

        if (cacheResources.transactionComponent() == null) {
            cache.transactionComponent(c -> c.locking("PESSIMISTIC")
                    .mode("NONE")
                    .stopTimeout(10000L));
        }

        if (cacheResources.binaryJdbcStore() == null
                && cacheResources.customStore() == null
                && cacheResources.fileStore() == null
                && cacheResources.mixedJdbcStore() == null
                && cacheResources.noneStore() == null
                && cacheResources.remoteStore() == null
                && cacheResources.stringJdbcStore() == null) {
            cache.noneStore();
        }

        return cache;
    }

    private static DistributedCache enableResourceDefaults(DistributedCache cache) {
        // from https://github.com/wildfly/wildfly/tree/10.0.0.CR4/clustering/infinispan/extension/src/main/java/org/jboss/as/clustering/infinispan/subsystem/ScheduledThreadPoolResourceDefinition.java#L346-L377
        DistributedCache.DistributedCacheResources cacheResources = cache.subresources();
        if (cacheResources.evictionComponent() == null) {
            cache.evictionComponent(c -> c.maxEntries(-1L)
                    .strategy("NONE"));
        }

        if (cacheResources.expirationComponent() == null) {
            cache.expirationComponent(c -> c.interval(60000L)
                    .lifespan(-1L)
                    .maxIdle(-1L));
        }

        if (cacheResources.lockingComponent() == null) {
            cache.lockingComponent(c -> c.acquireTimeout(15000L)
                    .concurrencyLevel(1000)
                    .isolation("READ_COMMITTED")
                    .striping(false));
        }

        if (cacheResources.transactionComponent() == null) {
            cache.transactionComponent(c -> c.locking("PESSIMISTIC")
                    .mode("NONE")
                    .stopTimeout(10000L));
        }

        if (cacheResources.binaryJdbcStore() == null
                && cacheResources.customStore() == null
                && cacheResources.fileStore() == null
                && cacheResources.mixedJdbcStore() == null
                && cacheResources.noneStore() == null
                && cacheResources.remoteStore() == null
                && cacheResources.stringJdbcStore() == null) {
            cache.noneStore();
        }

        if (cacheResources.partitionHandlingComponent() == null) {
            cache.partitionHandlingComponent(c -> c.enabled(true));
        }

        if (cacheResources.stateTransferComponent() == null) {
            cache.stateTransferComponent(c -> c.chunkSize(512)
                    .timeout(TimeUnit.MINUTES.toMillis(4)));
        }

        if (cacheResources.backupForComponent() == null) {
            cache.backupForComponent(c -> c.remoteCache("___defaultcache")
                    .remoteSite(null));
        }

        if (cacheResources.backupsComponent() == null) {
            cache.backupsComponent();
        }

        return cache;
    }

    private static ReplicatedCache enableResourceDefaults(ReplicatedCache cache) {
        // from https://github.com/wildfly/wildfly/tree/10.0.0.CR4/clustering/infinispan/extension/src/main/java/org/jboss/as/clustering/infinispan/subsystem/ScheduledThreadPoolResourceDefinition.java#L346-L377
        ReplicatedCache.ReplicatedCacheResources cacheResources = cache.subresources();
        if (cacheResources.evictionComponent() == null) {
            cache.evictionComponent(c -> c.maxEntries(-1L)
                    .strategy("NONE"));
        }

        if (cacheResources.expirationComponent() == null) {
            cache.expirationComponent(c -> c.interval(60000L)
                    .lifespan(-1L)
                    .maxIdle(-1L));
        }

        if (cacheResources.lockingComponent() == null) {
            cache.lockingComponent(c -> c.acquireTimeout(15000L)
                    .concurrencyLevel(1000)
                    .isolation("READ_COMMITTED")
                    .striping(false));
        }

        if (cacheResources.transactionComponent() == null) {
            cache.transactionComponent(c -> c.locking("PESSIMISTIC")
                    .mode("NONE")
                    .stopTimeout(10000L));
        }

        if (cacheResources.binaryJdbcStore() == null
                && cacheResources.customStore() == null
                && cacheResources.fileStore() == null
                && cacheResources.mixedJdbcStore() == null
                && cacheResources.noneStore() == null
                && cacheResources.remoteStore() == null
                && cacheResources.stringJdbcStore() == null) {
            cache.noneStore();
        }

        if (cacheResources.partitionHandlingComponent() == null) {
            cache.partitionHandlingComponent(c -> c.enabled(true));
        }

        if (cacheResources.stateTransferComponent() == null) {
            cache.stateTransferComponent(c -> c.chunkSize(512)
                    .timeout(TimeUnit.MINUTES.toMillis(4)));
        }

        if (cacheResources.backupForComponent() == null) {
            cache.backupForComponent(c -> c.remoteCache("___defaultcache")
                    .remoteSite(null));
        }

        if (cacheResources.backupsComponent() == null) {
            cache.backupsComponent();
        }

        return cache;
    }

    private boolean defaultFraction = false;
}
