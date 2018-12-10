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
package org.wildfly.swarm.infinispan.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.msc.service.ServiceActivator;
import org.wildfly.swarm.config.EJB3;
import org.wildfly.swarm.config.JPA;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.infinispan.cache_container.LockingComponent;
import org.wildfly.swarm.config.infinispan.cache_container.TransactionComponent;
import org.wildfly.swarm.infinispan.InfinispanFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

import static org.wildfly.swarm.infinispan.InfinispanMessages.MESSAGES;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class InfinispanCustomizer implements Customizer {

    private static final String DEFAULT = "default";

    private static final String PASSIVATION = "passivation";

    @Inject
    private InfinispanFraction fraction;

    @Inject
    private Instance<Undertow> undertow;

    @Inject
    private Instance<EJB3> ejb;

    @Inject
    private Instance<JPA> jpa;

    @Override
    public void customize() {
        if (this.fraction.isDefaultFraction()) {
            localCustomization();
        }
    }

    private void localCustomization() {
        this.fraction.cacheContainer("server",
                cc -> cc.defaultCache(DEFAULT)
                        .module("org.wildfly.clustering.server")
                        .localCache(DEFAULT, c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.BATCH))));

        if (!this.undertow.isUnsatisfied()) {
            this.fraction.cacheContainer("web",
                    cc -> cc.defaultCache(PASSIVATION)
                            .module("org.wildfly.clustering.web.infinispan")
                            .localCache(PASSIVATION,
                                    c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                            .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                            .fileStore(fs -> fs.passivation(true).purge(false))));
        }

        if (!this.ejb.isUnsatisfied()) {
            this.fraction.cacheContainer("ejb",
                    cc -> cc.alias("sfsb")
                            .defaultCache(PASSIVATION)
                            .module("org.wildfly.clustering.ejb.infinispan")
                            .localCache(PASSIVATION,
                                    c -> c.lockingComponent(lc -> lc.isolation(LockingComponent.Isolation.REPEATABLE_READ))
                                            .transactionComponent(tc -> tc.mode(TransactionComponent.Mode.BATCH))
                                            .fileStore(fs -> fs.passivation(true).purge(false))));
        }

        if (!this.jpa.isUnsatisfied()) {
            this.fraction.cacheContainer("hibernate",
                    cc -> cc.module("org.infinispan.hibernate-cache")
                            .localCache("entity",
                                    c -> c.transactionComponent(t -> t.mode(TransactionComponent.Mode.NON_XA))
                                            .objectMemory(om -> om.size(10000L))
                                            .expirationComponent(e -> e.maxIdle(100000L)))
                            .localCache("local-query",
                                    c -> c.objectMemory(om -> om.size(10000L))
                                            .expirationComponent(e -> e.maxIdle(100000L)))
                            .localCache("timestamps"));
        }

    }

    @Produces
    @Dependent
    public ServiceActivator defaultActivator() {
        return new CacheActivator("server", CacheActivator.Type.DEFAULT_CACHE);
    }

    @Produces
    @Dependent
    public ServiceActivator undertowActivator() {
        return createActivatorIfSatisfied(this.undertow, "web", CacheActivator.Type.DEFAULT_CACHE);
    }

    @Produces
    @Dependent
    public ServiceActivator ejbActivator() {
        return createActivatorIfSatisfied(this.ejb, "ejb", CacheActivator.Type.DEFAULT_CACHE);
    }

    @Produces
    @Dependent
    public ServiceActivator jpaActivator() {
        return createActivatorIfSatisfied(this.jpa, "hibernate", CacheActivator.Type.CACHE_CONTAINER_CONFIGURATION);
    }

    private ServiceActivator createActivatorIfSatisfied(Instance instance, String cacheContainer, CacheActivator.Type type) {
        if (instance.isUnsatisfied()) {
            MESSAGES.skippingCacheActivation(cacheContainer);
            return null;
        } else {
            return new CacheActivator(cacheContainer, type);
        }
    }
}
