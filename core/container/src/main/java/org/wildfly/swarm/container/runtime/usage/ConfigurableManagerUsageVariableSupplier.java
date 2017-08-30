package org.wildfly.swarm.container.runtime.usage;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.wildfly.swarm.container.runtime.ConfigurableHandle;
import org.wildfly.swarm.container.runtime.ConfigurableManager;

/**
 * Created by bob on 8/30/17.
 */
@ApplicationScoped
public class ConfigurableManagerUsageVariableSupplier implements UsageVariableSupplier {

    @Inject
    public ConfigurableManagerUsageVariableSupplier(ConfigurableManager manager) {
        this.manager = manager;
    }

    @Override
    public Object valueOf(String name) throws Exception {
        Optional<ConfigurableHandle> configurable = this.manager.configurables().stream()
                .filter(e -> e.key().toString().equals(name))
                .findFirst();

        if (configurable.isPresent()) {
            return configurable.get().currentValue();
        }

        return null;
    }

    private final ConfigurableManager manager;
}
