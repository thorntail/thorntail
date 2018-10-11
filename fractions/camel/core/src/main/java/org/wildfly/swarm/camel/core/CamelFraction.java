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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.jboss.gravia.utils.IllegalArgumentAssertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;
import org.wildfly.swarm.spi.api.annotations.WildFlySubsystem;

@WildFlyExtension(module = "org.wildfly.extension.camel")
@WildFlySubsystem("camel")
@Configurable("thorntail.camel")
public final class CamelFraction implements Fraction<CamelFraction> {

    public static final Logger LOGGER = LoggerFactory.getLogger("org.wildfly.swarm.camel");

    public CamelFraction() {
    }

    private final Map<String, RouteBuilder> routeBuilders = new LinkedHashMap<>();

    public CamelFraction addRouteBuilder(RouteBuilder builder) {
        return addRouteBuilder(null, builder);
    }

    @SuppressWarnings("unchecked")
    public CamelFraction addRouteBuilder(String name, RouteBuilder builder) {
        IllegalArgumentAssertion.assertNotNull(builder, "builder");
        routeBuilders.put(name, builder);
        return this;
    }

    public Map<String, RouteBuilder> getRouteBuilders() {
        return Collections.unmodifiableMap(routeBuilders);
    }

    public void context(String key, String path) {
        this.contexts.put(key, path);
    }

    public void contexts(Map<String, String> contexts) {
        this.contexts.clear();
        this.contexts.putAll(contexts);
    }

    public Map<String, String> contexts() {
        return this.contexts;
    }

    private Map<String, String> contexts = new HashMap<>();

}
