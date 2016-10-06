/*
 * #%L
 * Camel Core :: Main
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCamelFraction<T extends AbstractCamelFraction<T>> {

    public final static Logger LOGGER = LoggerFactory.getLogger("org.wildfly.swarm.camel");

    private final Map<String, RouteBuilder> routeBuilders = new LinkedHashMap<>();

    public T addRouteBuilder(RouteBuilder builder) {
        return addRouteBuilder(null, builder);
    }

    @SuppressWarnings("unchecked")
    public T addRouteBuilder(String name, RouteBuilder builder) {
        IllegalArgumentAssertion.assertNotNull(builder, "builder");
        routeBuilders.put(name, builder);
        return (T)this;
    }

    public Map<String, RouteBuilder> getRouteBuilders() {
        return Collections.unmodifiableMap(routeBuilders);
    }
}
