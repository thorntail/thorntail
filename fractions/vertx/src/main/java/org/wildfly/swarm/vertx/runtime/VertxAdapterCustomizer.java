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
package org.wildfly.swarm.vertx.runtime;

import java.util.Arrays;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.config.resource.adapters.ResourceAdapter;
import org.wildfly.swarm.config.resource.adapters.resource_adapter.ConfigProperties;
import org.wildfly.swarm.resource.adapters.ResourceAdapterFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.vertx.VertxFraction;

/**
 * @author Ken Finnigan
 */
@Pre
@Singleton
public class VertxAdapterCustomizer implements Customizer {
    @Inject
    Instance<ResourceAdapterFraction> resourceAdapterFractionInstance;

    @Inject
    Instance<VertxFraction> vertxFractionInstance;

    @Override
    public void customize() {
        if (!resourceAdapterFractionInstance.isUnsatisfied() && !vertxFractionInstance.isUnsatisfied()) {
            VertxFraction vertxFraction = vertxFractionInstance.get();

            if (!vertxFraction.isAdapterDeploymentInhibited()) {
                resourceAdapterFractionInstance.get()
                        .resourceAdapter("vertx-ra", ra -> {
                            ra.module("io.vertx.jca:ra")
                                    .transactionSupport(ResourceAdapter.TransactionSupport.NOTRANSACTION)
                                    .connectionDefinitions("VertxConnectionFactory", c -> {
                                        c.className("io.vertx.resourceadapter.impl.VertxManagedConnectionFactory")
                                                .jndiName(vertxFraction.jndiName())
                                                .enabled(true)
                                                .configProperties(
                                                        Arrays.asList(
                                                                new ConfigProperties("clusterHost").value(vertxFraction.clusterHost()),
                                                                new ConfigProperties("clusterPort").value(String.valueOf(vertxFraction.clusterPort()))
                                                        )
                                                );
                                    });
                        });
            }
        }
    }
}
