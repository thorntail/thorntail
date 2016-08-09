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
package org.wildfly.swarm.monitor.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Ken Finnigan
 */
@Post
@ApplicationScoped
public class UndertowFilterCustomizer implements Customizer {
    @Inject
    @Any
    Instance<UndertowFraction> undertowFractionInstance;

    @Override
    public void customize() {
        if (!undertowFractionInstance.isUnsatisfied()) {
            UndertowFraction undertow = undertowFractionInstance.get();

            undertow.filterConfiguration();
            undertow.subresources().filterConfiguration()
                    .customFilter("wfs-monitor", customFilter -> {
                        customFilter.module("org.wildfly.swarm.monitor:runtime");
                        customFilter.className("org.wildfly.swarm.monitor.runtime.SecureHttpContexts");
                    });

            undertow.subresources().server("default-server")
                    .subresources().host("default-host")
                    .filterRef("wfs-monitor", f -> {
                        f.priority(100);
                    });


        } else {
            throw new RuntimeException("The monitor fraction requires the undertow fraction!");
        }
    }
}
