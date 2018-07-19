/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.metrics.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * Install the http handler for MicroProfile Metrics.
 * Could perhaps be turned into a real (JAX-RS) WebApp later(?)
 *
 * @author Heiko W. Rupp
 */
@Post
@ApplicationScoped
public class MetricsInstaller implements Customizer {

    @Inject
    Instance<UndertowFraction> undertowFractionInstance;

    public void customize() {
        if (!undertowFractionInstance.isUnsatisfied()) {
            UndertowFraction undertow = undertowFractionInstance.get();

            if (undertow.subresources().filterConfiguration() == null) {
                undertow.filterConfiguration();
            }
            undertow.subresources().filterConfiguration()
                    .customFilter("wfs-mp-metrics", customFilter -> {
                        customFilter.module("org.wildfly.swarm.microprofile.metrics:runtime");
                        customFilter.className("org.wildfly.swarm.microprofile.metrics.runtime.MetricsHttpHandler");
                    });

            undertow.subresources().server("default-server")
                    .subresources().host("default-host")
                    .filterRef("wfs-mp-metrics", f -> {
                        f.priority(101);
                    });
        } else {
            throw new RuntimeException("The monitor fraction requires the undertow fraction!");
        }
    }

}
