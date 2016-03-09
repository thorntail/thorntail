/*
 * #%L
 * Wildfly Swarm :: Camel Core API
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.swarm.camel.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.container.Fraction;

public class CamelCoreFraction implements Fraction {

    public final static Logger LOGGER = LoggerFactory.getLogger("org.wildfly.swarm.camel");

    private final List<RouteBuilder> routeBuilders = new ArrayList<>();

    public CamelCoreFraction addRouteBuilder(RouteBuilder builder) {
        routeBuilders.add(builder);
        return this;
    }

    public List<RouteBuilder> getRouteBuilders() {
        return Collections.unmodifiableList(routeBuilders);
    }
}
