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
package org.wildfly.swarm.monitor;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Heiko Braun
 */
public class MonitorFraction implements Fraction {

    public MonitorFraction() {

    }

    @Override
    public void postInitialize(Container.PostInitContext initContext) {

        UndertowFraction undertow = (UndertowFraction) initContext.fraction("undertow");

        if (undertow != null) {
            undertow.filterConfiguration();
            undertow.subresources().filterConfiguration()
                    .customFilter("wfs-monitor", customFilter -> {
                        customFilter.module("org.wildfly.swarm.monitor:runtime");
                        customFilter.className("org.wildfly.swarm.monitor.runtime.MonitorEndpoints");
                    });

            undertow.subresources().server("default-server")
                    .subresources().host("default-host")
                    .filterRef("wfs-monitor");


        } else {
            throw new RuntimeException("The monitor fraction requires the undertow fraction!");
        }

    }
}
