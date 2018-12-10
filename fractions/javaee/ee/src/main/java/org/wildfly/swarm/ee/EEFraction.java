/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.ee;

import javax.annotation.PostConstruct;

import org.wildfly.swarm.config.EE;
import org.wildfly.swarm.config.ee.ContextService;
import org.wildfly.swarm.config.ee.DefaultBindingsServiceConsumer;
import org.wildfly.swarm.config.ee.ManagedExecutorService;
import org.wildfly.swarm.config.ee.ManagedScheduledExecutorService;
import org.wildfly.swarm.config.ee.ManagedThreadFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.jboss.as.ee")
@MarshalDMR
public class EEFraction extends EE<EEFraction> implements Fraction<EEFraction> {

    public static final String CONCURRENCY_CONTEXT_DEFAULT = "java:jboss/ee/concurrency/context/default";

    public static final String CONCURRENCY_FACTORY_DEFAULT = "java:jboss/ee/concurrency/factory/default";

    public static final String CONCURRENCY_EXECUTOR_DEFAULT = "java:jboss/ee/concurrency/executor/default";

    public static final String CONCURRENCY_SCHEDULER_DEFAULT = "java:jboss/ee/concurrency/scheduler/default";

    public static final String DEFAULT_KEY = "default";

    public static EEFraction createDefaultFraction() {
        return createDefaultFraction(null);
    }

    public static EEFraction createDefaultFraction(DefaultBindingsServiceConsumer config) {
        return new EEFraction().applyDefaults();
    }

    @PostConstruct
    public void postConstruct() {
        applyDefaults();
    }

    public EEFraction applyDefaults() {
        return applyDefaults(null);
    }

    @SuppressWarnings("unchecked")
    public EEFraction applyDefaults(DefaultBindingsServiceConsumer config) {
        specDescriptorPropertyReplacement(false)
                .contextService(new ContextService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_CONTEXT_DEFAULT)
                        .useTransactionSetupProvider(false)) // WildFly defaults to true, but that probably needs transactions
                .managedThreadFactory(new ManagedThreadFactory(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_FACTORY_DEFAULT)
                        .contextService(DEFAULT_KEY))
                .managedExecutorService(new ManagedExecutorService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_EXECUTOR_DEFAULT)
                        .contextService(DEFAULT_KEY)
                        .hungTaskThreshold(60000L)
                        .keepaliveTime(5000L))
                .managedScheduledExecutorService(new ManagedScheduledExecutorService(DEFAULT_KEY)
                        .jndiName(CONCURRENCY_SCHEDULER_DEFAULT)
                        .contextService(DEFAULT_KEY)
                        .hungTaskThreshold(60000L)
                        .keepaliveTime(3000L));

        defaultBindingsService((bindings) -> {
            bindings.contextService(CONCURRENCY_CONTEXT_DEFAULT);
            bindings.managedExecutorService(CONCURRENCY_EXECUTOR_DEFAULT);
            bindings.managedScheduledExecutorService(CONCURRENCY_SCHEDULER_DEFAULT);
            bindings.managedThreadFactory(CONCURRENCY_FACTORY_DEFAULT);
            if (config != null) {
                config.accept(bindings);
            }
        });

        return this;
    }
}
