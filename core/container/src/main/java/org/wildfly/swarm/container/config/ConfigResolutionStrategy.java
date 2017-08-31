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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.wildfly.swarm.spi.api.ConfigurationFilter;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * A preferred-order configuration resolution strategy.
 *
 * <p>The strategy uses either an explicitly-passed {@link Properties} object
 * or the System properties to augment any {@link ConfigNode} objects that have
 * been added.  The strategy will reflect any configuration from the {@code ConfigNode}
 * sequence back to the given properties.  If using the System properties approach,
 * this will actively change properties as returned by {@code System.getProperty(...)}
 * and similar methods.</p>
 *
 * <p>The given {@code ConfigNode} objects will be search in-order for the first match.</p>
 *
 * @author Bob McWhirter
 */
class ConfigResolutionStrategy {

    /**
     * Construct a strategy based upon Java System properties.
     */
    ConfigResolutionStrategy() {
        this(PropertiesManipulator.system());
    }

    /**
     * Construct a strategy based upon a given properties object.
     *
     * @param properties The properties.
     */
    ConfigResolutionStrategy(Properties properties) {
        this(properties == null ? PropertiesManipulator.system() : PropertiesManipulator.forProperties(properties));
    }

    private ConfigResolutionStrategy(PropertiesManipulator properties) {
        this.propertiesNode = PropertiesConfigNodeFactory.load(properties.getProperties());
        this.nodes.add(this.propertiesNode);
        this.properties = properties;
    }

    public void withFilter(ConfigurationFilter filter) {
        this.filters.add(filter);
    }

    void withProperties(Properties properties) {
        this.propertiesNode = PropertiesConfigNodeFactory.load(properties);
        this.nodes.add(this.propertiesNode);
        this.properties = PropertiesManipulator.forProperties(properties);
    }

    void withEnvironment(Map<String, String> environment) {
        this.nodes.add(EnvironmentConfigNodeFactory.load(environment));
    }

    /**
     * Add a {@code ConfigNode} to the search list.
     *
     * @param node The node to add.
     */
    void add(ConfigNode node) {
        this.nodes.add(node);
    }

    void defaults(ConfigNode defaults) {
        this.defaults = defaults;
    }

    void withProperty(String name, String value) {
        this.propertiesNode.recursiveChild(name, value);
    }

    /**
     * Activate the strategy.
     */
    void activate() {
        nodes().flatMap(e -> e.allKeysRecursively())
                .distinct()
                .forEach(key -> {
                    activate(key);
                });
    }

    Stream<ConfigNode> nodes() {
        if (this.defaults == null) {
            return this.nodes.stream();
        }

        return Stream.concat(this.nodes.stream(), Stream.of(this.defaults));
    }

    /**
     * Activate a given key.
     *
     * @param key The key to activate.
     */
    private void activate(ConfigKey key) {
        optionalValueOf(key).ifPresent((v) -> {
            this.properties.setProperty(key.name(), v.toString());
        });
    }

    private void deactivate(ConfigKey key) {
        optionalValueOf(key).ifPresent((v) -> {
            this.properties.clearProperty(key.name());
        });
    }

    /**
     * Retrieve the configuration value for a key.
     *
     * @param key The possibly complex key.
     * @return The value, otherwise {@code null}.
     */
    public Object valueOf(ConfigKey key) {
        return optionalValueOf(key).orElse(null);
    }

    Optional<Object> optionalValueOf(ConfigKey key) {
        return nodes()
                .map(e -> e.valueOf(key))
                .filter(Objects::nonNull)
                .map(v -> filter(key, v))
                .filter(Objects::nonNull)
                .findFirst();
    }

    Object filter(ConfigKey key, Object value) {
        for (ConfigurationFilter filter : this.filters) {
            value = filter.filter(key.propertyName(), value);
        }
        return value;
    }

    Stream<ConfigKey> allKeysRecursively() {
        return nodes().flatMap(e -> e.allKeysRecursively());

    }

    List<SimpleKey> simpleSubkeysOf(ConfigKey prefix) {
        return nodes()
                .map(e -> e.descendant(prefix))
                .filter(Objects::nonNull)
                .flatMap(e -> e.childrenKeys().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    boolean hasKeyOrSubkeys(ConfigKey prefix) {
        return nodes()
                .map(e -> e.descendant(prefix))
                .anyMatch(Objects::nonNull);
    }

    Properties asProperties() {
        return this.properties.getProperties();
    }

    private PropertiesManipulator properties;

    private List<ConfigNode> nodes = new ArrayList<>();

    private ConfigNode defaults;

    private ConfigNode propertiesNode;

    private List<ConfigurationFilter> filters = new ArrayList<>();

}
