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
package org.wildfly.swarm.container.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.spi.api.ConfigurationFilter;
import org.wildfly.swarm.spi.api.config.Builder;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.Resolver;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * A view of merged/activated configurations.
 *
 * <p>If an explicit properties object is provided ({@link #withProperties(Properties)}, then
 * the System properties will be completely ignored.  If not, then System properties will be
 * inspected, and any non-property based configuration will be reflect <b>back</b> into the
 * live and active System properties.</p>
 *
 * @author Bob McWhirter
 */
@Vetoed
public class ConfigViewImpl implements ConfigView {

    /**
     * Construct a new view.
     */
    public ConfigViewImpl() {
        this.strategy = new ConfigResolutionStrategy();
    }

    /**
     * Supply explicit properties object for introspection and outrespection.
     *
     * @param properties The properties.
     * @return This view.
     */
    public ConfigViewImpl withProperties(Properties properties) {
        if (properties != null) {
            this.properties = properties;
            this.strategy.withProperties(properties);
        }
        return this;
    }

    public ConfigViewImpl withProperty(String name, String value) {
        this.strategy.withProperty(name, value);
        return this;
    }

    public ConfigViewImpl withEnvironment(Map<String, String> environment) {
        if (environment != null) {
            this.strategy.withEnvironment(environment);
        } else {
            this.strategy.withEnvironment(System.getenv());
        }
        return this;

    }

    /**
     * Define the tail-end, default {@code ConfigNode}.
     *
     * @param defaultConfig The defaults.
     * @return This view.
     */
    public ConfigViewImpl withDefaults(ConfigNode defaultConfig) {
        this.strategy.defaults(defaultConfig);
        return this;
    }

    /**
     * Register a named {@code ConfigNode}.
     *
     * @param name   The name to register.
     * @param config THe node to register.
     */
    public synchronized void register(String name, ConfigNode config) {

        List<ConfigNode> nodes = this.registry.get(name);
        if (nodes == null) {
            nodes = new ArrayList<>();
            this.registry.put(name, nodes);
        }

        nodes.add(config);
    }

    /**
     * Return the list of all registered node names.
     *
     * @return The list of registered node names.
     */
    public Set<String> registered() {
        return this.registry.keySet();
    }

    /**
     * Retrieve a configuration value by key.
     *
     * @param key The possibly complex key.
     * @return The value if present, otherwise {@code null}.
     */
    public Object valueOf(ConfigKey key) {
        return this.strategy.valueOf(key);
    }

    public void withFilter(ConfigurationFilter filter) {
        this.strategy.withFilter(filter);
    }

    void withProfile(String... names) {
        for (String name : names) {
            List<ConfigNode> nodes = this.registry.get(name);
            if (nodes != null) {
                nodes.forEach(node -> {
                    this.strategy.add(node);
                });
            }
        }

    }

    void withProfile(List<String> names) {
        withProfile(names.toArray(new String[]{}));
    }

    public void activate() {
        this.strategy.activate();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------


    @Override
    public Properties asProperties() {
        return this.strategy.asProperties();
    }

    @Override
    public List<SimpleKey> simpleSubkeys(ConfigKey prefix) {
        return this.strategy.simpleSubkeysOf(prefix);
    }

    @Override
    public boolean hasKeyOrSubkeys(ConfigKey subPrefix) {
        return this.strategy.hasKeyOrSubkeys(subPrefix);
    }

    @Override
    public Resolver<?> resolverFor(ConfigKey key) {
        return new Builder<>(this, key);
    }

    public Stream<ConfigKey> allKeysRecursively() {
        return this.strategy.allKeysRecursively();
    }

    private Properties properties;

    private Map<String, List<ConfigNode>> registry = new HashMap<>();

    private ConfigResolutionStrategy strategy;


}
