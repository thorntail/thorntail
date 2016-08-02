package org.wildfly.swarm.infinispan;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.EJB3;
import org.wildfly.swarm.config.JGroups;
import org.wildfly.swarm.config.JPA;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.infinispan.Mode;
import org.wildfly.swarm.config.infinispan.cache_container.EvictionComponent;
import org.wildfly.swarm.config.infinispan.cache_container.LockingComponent;
import org.wildfly.swarm.config.infinispan.cache_container.TransactionComponent;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Post;

/**
 * @author Bob McWhirter
 */
@Post
@Singleton
public class InfinispanCustomizer implements Customizer {

    @Inject @Any
    private InfinispanFraction fraction;

    @Inject @Any
    private Instance<JGroups> jgroups;

    @Inject @Any
    private Instance<Undertow> undertow;

    @Inject @Any
    private Instance<EJB3> ejb;

    @Inject @Any
    private Instance<JPA> jpa;



    @Override
    public void customize() {

        if ( this.fraction.isDefaultFraction() ) {
            if ( ! this.jgroups.isUnsatisfied() ) {
                clusteredCustomization();
            } else {
                localCustomization();
            }
        }

    }

    private void clusteredCustomization() {
        this.fraction.cacheContainer("server",
                cc -> cc.defaultCache("default")
                        .alias("singleton")
                        .alias("cluster")
                        .jgroupsTransport(t -> t.lockTimeout(60000L))
                        .replicatedCache("default",
                                c -> c.mode(Mode.SYNC)
                                        .transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH))));
        if (!this.undertow.isUnsatisfied()) {
            this.fraction.cacheContainer("web",
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

        if (!this.ejb.isUnsatisfied()) {
            this.fraction.cacheContainer("ejb",
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

        if (!this.jpa.isUnsatisfied()) {
            this.fraction.cacheContainer("hibernate",
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

    }

    private void localCustomization() {
        this.fraction.cacheContainer("server",
                cc -> cc.defaultCache("default")
                        .localCache("default", c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH)))
                        .remoteCommandThreadPool());

        if (!this.undertow.isUnsatisfied()) {
            this.fraction.cacheContainer("web",
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

        if (!this.ejb.isUnsatisfied()) {
            this.fraction.cacheContainer("ejb",
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

        if (!this.jpa.isUnsatisfied()) {
            this.fraction.cacheContainer("hibernate",
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

    }
}
