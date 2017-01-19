package org.wildfly.swarm.container.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

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

    }

    /**
     * Supply explicit properties object for introspection and outrespection.
     *
     * @param properties The properties.
     * @return This view.
     */
    public ConfigViewImpl withProperties(Properties properties) {
        this.properties = properties;
        return this;

    }

    /**
     * Define the tail-end, default {@code ConfigNode}.
     *
     * @param defaultConfig The defaults.
     * @return This view.
     */
    public ConfigViewImpl withDefaults(ConfigNode defaultConfig) {
        this.defaultConfig = defaultConfig;
        return this;
    }

    /**
     * Register a named {@code ConfigNode}.
     *
     * @param name   The name to register.
     * @param config THe node to register.
     */
    public void register(String name, ConfigNode config) {
        this.registry.put(name, config);
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

    /**
     * Activate this view with the given node names.
     *
     * <p>This method should be called only after defaults, properties
     * and all relevant nodes have been registered.</p>
     *
     * @param names The names to activate.
     */
    public void activate(String... names) {
        this.strategy = new ConfigResolutionStrategy(this.properties);

        for (String name : names) {
            ConfigNode each = this.registry.get(name);
            if (each != null) {
                this.strategy.add(each);
            }
        }

        if (this.defaultConfig != null) {
            this.strategy.add(this.defaultConfig);
        }

        this.strategy.activate();
    }

    public void activate(List<String> names) {
        activate(names.toArray(new String[]{}));
    }


    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------


    @Override
    public Properties asProperties() {
        return this.strategy.asProperties();
    }

    @Override
    public Set<SimpleKey> simpleSubkeys(ConfigKey prefix) {
        return this.strategy.simpleSubkeysOf(prefix);
    }

    @Override
    public boolean hasKeyOrSubkeys(ConfigKey subPrefix) {
        return this.strategy.hasKeyOrSubkeys(subPrefix);
    }

    public Resolver<String> resolve(ConfigKey key) {
        return new Builder<>(this, key).as(String.class);
    }

    private Properties properties;

    private ConfigNode defaultConfig;

    private Map<String, ConfigNode> registry = new HashMap<>();

    private ConfigResolutionStrategy strategy;


}
