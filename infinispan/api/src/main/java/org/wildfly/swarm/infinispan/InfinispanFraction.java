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
package org.wildfly.swarm.infinispan;

import org.wildfly.swarm.config.Infinispan;
import org.wildfly.swarm.config.infinispan.Mode;
import org.wildfly.swarm.config.infinispan.cache_container.EvictionComponent;
import org.wildfly.swarm.config.infinispan.cache_container.LockingComponent;
import org.wildfly.swarm.config.infinispan.cache_container.TransactionComponent;
import org.wildfly.swarm.spi.api.Fraction;

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
    public void postInitialize(Fraction.PostInitContext initContext) {
        if (this.defaultFraction) {
            if (initContext.hasFraction("jgroups")) {
                clusteredDefaultFraction(initContext);
            } else {
                localDefaultFraction(initContext);
            }
        }
    }

    protected InfinispanFraction markDefaultFraction() {
        this.defaultFraction = true;

        return this;
    }

    private InfinispanFraction clusteredDefaultFraction(Fraction.PostInitContext initContext) {
        cacheContainer("server",
                       cc -> cc.defaultCache("default")
                               .alias("singleton")
                               .alias("cluster")
                               .jgroupsTransport(t -> t.lockTimeout(60000L))
                               .replicatedCache("default",
                                                c -> c.mode(Mode.SYNC)
                                                        .transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH))));
        if (initContext.hasFraction("undertow")) {
            cacheContainer("web",
                           cc -> cc.defaultCache("dist")
                                   .jgroupsTransport(t -> t.lockTimeout(60000L))
                                   .distributedCache("dist",
                                                     c -> c.mode(Mode.ASYNC)
                                                             .l1Lifespan(0L)
                                                             .owners(2)
                                                             .lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                             .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                                             .fileStore()));
        }

        if (initContext.hasFraction("ejb")) {
            cacheContainer("ejb",
                           cc -> cc.defaultCache("dist")
                                   .alias("sfsb")
                                   .jgroupsTransport(t -> t.lockTimeout(60000L))
                                   .distributedCache("dist",
                                                     c -> c.mode(Mode.ASYNC)
                                                             .l1Lifespan(0L)
                                                             .owners(2)
                                                             .lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                             .transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH))
                                                             .fileStore()));

        }

        if (initContext.hasFraction("jpa")) {
            cacheContainer("hibernate",
                           cc -> cc.defaultCache("local-query")
                                   .jgroupsTransport(t -> t.lockTimeout(60000L))
                                   .localCache("local-query",
                                               c -> c.evictionComponent(ec -> ec.maxEntries(10000L).strategy(EvictionComponent.Strategy.LRU))
                                                       .expirationComponent(ec -> ec.maxIdle(100000L)))
                                   .invalidationCache("entity",
                                                      c -> c.mode(Mode.SYNC)
                                                              .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.NON_XA))
                                                              .evictionComponent(ec -> ec.maxEntries(10000L).strategy(EvictionComponent.Strategy.LRU))
                                                              .expirationComponent(ec -> ec.maxIdle(100000L)))
                                   .replicatedCache("timestamps", c -> c.mode(Mode.ASYNC)));
        }

        return this;
    }

    private InfinispanFraction localDefaultFraction(Fraction.PostInitContext initContext) {
        cacheContainer("server",
                       cc -> cc.defaultCache("default")
                               .localCache("default", c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH)))
                               .remoteCommandThreadPool());

        if (initContext.hasFraction("undertow")) {
            cacheContainer("web",
                           cc -> cc.defaultCache("passivation")
                                   .localCache("passivation",
                                               c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                       .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                                       .fileStore(fs -> fs.passivation(true).purge(false)))
                                   .localCache("persistent",
                                               c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                       .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                                       .fileStore(fs -> fs.passivation(false).purge(false))));
        }

        if (initContext.hasFraction("ejb")) {
            cacheContainer("ejb",
                           cc -> cc.alias("sfsb")
                                   .defaultCache("passivation")
                                   .localCache("passivation",
                                               c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                       .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                                       .fileStore(fs -> fs.passivation(true).purge(false)))
                                   .localCache("persistent",
                                               c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                                       .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                                       .fileStore(fs -> fs.passivation(false).purge(false))));
        }

        if (initContext.hasFraction("jpa")) {
            cacheContainer("hibernate",
                           cc -> cc.defaultCache("local-query")
                                   .localCache("entity",
                                               c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.NON_XA))
                                                       .evictionComponent(e -> e.strategy(EvictionComponent.Strategy.LRU).maxEntries(10000L))
                                                       .expirationComponent(e -> e.maxIdle(100000L)))
                                   .localCache("immutable-entity",
                                               c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.NON_XA))
                                                       .evictionComponent(e -> e.strategy(EvictionComponent.Strategy.LRU).maxEntries(10000L))
                                                       .expirationComponent(e -> e.maxIdle(100000L)))
                                   .localCache("local-query",
                                               c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.NON_XA))
                                                       .evictionComponent(e -> e.strategy(EvictionComponent.Strategy.LRU).maxEntries(10000L))
                                                       .expirationComponent(e -> e.maxIdle(100000L)))
                                   .localCache("timestamps"));
        }

        return this;
    }

    private boolean defaultFraction = false;
}
