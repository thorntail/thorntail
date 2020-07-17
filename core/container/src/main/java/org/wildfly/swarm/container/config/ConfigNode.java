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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigTree;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * A configuration node capable of having a direct value in addition to key/value children.
 *
 * @author Bob McWhirter
 */
public class ConfigNode implements ConfigTree {

    public ConfigNode() {
    }

    ConfigNode(Object value) {
        this.value = value;
    }

    /**
     * Set the value of an immediate child.
     *
     * @param key   The simple child key.
     * @param value The value to set.
     */
    public void child(SimpleKey key, Object value) {
        if (value instanceof ConfigNode) {
            this.children.put(key, (ConfigNode) value);
        } else {
            if (this.children.containsKey(key)) {
                this.children.get(key).value = value;
            } else {
                this.children.put(key, new ConfigNode(value));
            }
        }
    }

    /**
     * Set the value of an immediate child.
     *
     * @param key   The simple child key.
     * @param value The value to set.
     */
    public void child(String key, Object value) {
        child(new SimpleKey(key), value);
    }

    /**
     * Set the value of a descendant.
     *
     * <p>Any intermediate leafs will be created as-needed.</p>
     *
     * @param key   The possibly-complex key to a descendant.
     * @param value The value to set.
     */
    public void recursiveChild(String key, Object value) {
        recursiveChild(ConfigKey.parse(key), value);
    }

    /**
     * Set the value of a descendant.
     *
     * <p>Any intermediate leafs will be created as-needed.</p>
     *
     * @param key   The possibly-complex key to a descendant.
     * @param value The value to set.
     */
    public void recursiveChild(ConfigKey key, Object value) {
        SimpleKey head = key.head();

        if (head == ConfigKey.EMPTY) {
            value(value);
        }

        ConfigKey rest = key.subkey(1);

        if (rest == ConfigKey.EMPTY) {
            child(head, value);
        } else {
            ConfigNode child = child(head);
            if (child == null) {
                child = new ConfigNode();
                child(head, child);
            }
            child.recursiveChild(rest, value);
        }
    }

    ConfigNode descendant(ConfigKey key) {
        SimpleKey head = key.head();

        if (head == ConfigKey.EMPTY) {
            return this;
        }

        ConfigKey rest = key.subkey(1);

        ConfigNode child = child(head);
        if (child == null) {
            return null;
        }

        return child.descendant(rest);
    }

    /**
     * Retrieve the immediate child node.
     *
     * @param key The child's key.
     * @return The node or {@code null} is none present.
     */
    ConfigNode child(SimpleKey key) {
        ConfigNode child = this.children.get(key);
        return child;
    }

    /**
     * Retrieve the immediate child node.
     *
     * @param key The child's key.
     * @return The node or {@code null} is none present.
     */
    ConfigNode child(String key) {
        return child(new SimpleKey(key));
    }

    /**
     * Retrieve all immediate children keys.
     *
     * @return All immediate children keys.
     */
    public Set<SimpleKey> childrenKeys() {
        return this.children.keySet();
    }

    /**
     * Retrieve all descendent keys.
     *
     * @return A stream of all descendent keys.
     */
    public Stream<ConfigKey> allKeysRecursively() {
        Stream<ConfigKey> str = Stream.empty();
        if (this.value != null) {
            str = Stream.of(ConfigKey.EMPTY);
        }
        str = Stream.concat(str,
                this.children.entrySet()
                        .stream()
                        .flatMap((kv) -> {
                            ConfigKey key = kv.getKey();
                            Object value = kv.getValue();
                            if (value instanceof ConfigNode) {
                                return ((ConfigNode) value).allKeysRecursively()
                                        .map(childKey -> key.append(childKey));
                            }

                            return Stream.empty();
                        }));

        return str;
    }

    /**
     * Set the value on this node.
     *
     * @param value The value.
     */
    void value(Object value) {
        if (value instanceof ConfigNode) {
            throw new RuntimeException("Cannot set config-node as a value of a tree config-node");
        }

        this.value = value;
    }


    /**
     * Retrieve a value.
     *
     * @param key The possibly-complex key of the value to retrieve.
     * @return The value of {@code null} if none.
     */
    public Object valueOf(ConfigKey key) {

        SimpleKey head = key.head();

        if (head == ConfigKey.EMPTY) {
            if (this.value == null && this.children != null && !this.children.isEmpty()) {
                return this;
            }
            return this.value;
        }

        ConfigNode child = child(head);

        if (child != null) {
            ConfigKey rest = key.subkey(1);
            return child.valueOf(rest);
        }

        return null;
    }

    protected boolean isListLike() {
        return this.children.keySet().stream()
                .allMatch(e -> e.toString().matches("^[0-9]*$"));
    }

    public Object asObject() {
        if (this.value != null) {
            return this.value;
        }

        if (isListLike()) {
            return asList();
        }

        return asMap();
    }

    public List asList() {
        return this.children.values().stream()
                .map(e -> e.asObject())
                .collect(Collectors.toList());
    }

    public Map asMap() {
        Map<String,Object> map = new HashMap<>();

        this.children.entrySet()
                .forEach(entry -> {
                    map.put(entry.getKey().toString(), entry.getValue().asObject());
                });

        return map;
    }

    @Override
    public Properties asProperties() {
        Properties properties = new Properties();

        this.children.entrySet()
            .stream()
            .filter(entry -> entry.getValue().value != null)
            .forEach(entry -> {
                properties.setProperty(entry.getKey().toString(), entry.getValue().value.toString());
            });

        return properties;
    }

    public String toString() {
        return "[ConfigNode: (" + System.identityHashCode(this.children) + ") children=" + this.children + "; value=" + this.value + "]";
    }

    private Map<SimpleKey, ConfigNode> children = new LinkedHashMap<>();

    private Object value;

}
