/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.openapi.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author eric.wittmann@gmail.com
 */
@Post
@ApplicationScoped
public class OpenApiUndertowFilterCustomizer implements Customizer {

    @Inject
    Instance<UndertowFraction> undertowFractionInstance;

    @Override
    public void customize() {
        if (!undertowFractionInstance.isUnsatisfied()) {
            UndertowFraction undertow = undertowFractionInstance.get();

            if (undertow.subresources().filterConfiguration() == null) {
                undertow.filterConfiguration();
            }
            undertow.subresources().filterConfiguration()
                    .customFilter("wfs-openapi", customFilter -> {
                        customFilter.module("org.wildfly.swarm.microprofile.openapi:runtime");
                        customFilter.className("org.wildfly.swarm.microprofile.openapi.runtime.OpenApiHttpHandler");
                    });

            undertow.subresources().server("default-server")
                    .subresources().host("default-host")
                    .filterRef("wfs-openapi", f -> {
                        f.priority(100);
                    });


        } else {
            throw new RuntimeException("The microprofile-openapi fraction requires the undertow fraction!");
        }
    }
}
