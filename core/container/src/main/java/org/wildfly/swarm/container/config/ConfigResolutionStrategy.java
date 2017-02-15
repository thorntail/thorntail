package org.wildfly.swarm.container.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.nodes.add(PropertiesConfigNodeFactory.load(properties.getProperties()));
        this.properties = properties;
    }

    /**
     * Add a {@code ConfigNode} to the search list.
     *
     * @param node The node to add.
     */
    void add(ConfigNode node) {
        this.nodes.add(node);
    }

    /**
     * Activate the strategy.
     */
    void activate() {
        this.nodes
                .stream()
                .flatMap(e -> e.allKeysRecursively())
                .distinct()
                .forEach(key -> {
                    activate(key);
                });
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
        return this.nodes.stream()
                .map(e -> e.valueOf(key))
                .filter(Objects::nonNull)
                .findFirst();
    }

    Stream<ConfigKey> allKeysRecursively() {
        return this.nodes
                .stream()
                .flatMap(e -> e.allKeysRecursively());
    }

    Set<SimpleKey> simpleSubkeysOf(ConfigKey prefix) {
        return this.nodes
                .stream()
                .map(e -> e.descendant(prefix))
                .filter(Objects::nonNull)
                .flatMap(e -> e.childrenKeys().stream())
                .collect(Collectors.toSet());
    }

    boolean hasKeyOrSubkeys(ConfigKey prefix) {
        return this.nodes
                .stream()
                .map(e -> e.descendant(prefix))
                .anyMatch(Objects::nonNull);
    }

    Properties asProperties() {
        return this.properties.getProperties();
    }

    private final PropertiesManipulator properties;

    private List<ConfigNode> nodes = new ArrayList<>();

}
