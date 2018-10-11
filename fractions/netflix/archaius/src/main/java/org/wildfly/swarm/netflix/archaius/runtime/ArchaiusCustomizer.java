package org.wildfly.swarm.netflix.archaius.runtime;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.netflix.config.ConfigurationManager;
import org.apache.commons.configuration.AbstractConfiguration;
import org.wildfly.swarm.container.runtime.ConfigurableHandle;
import org.wildfly.swarm.container.runtime.ConfigurableManager;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class ArchaiusCustomizer implements Customizer {

    @Inject
    ConfigurableManager configurableManager;

    @Inject
    @Any
    Instance<ArchaiusLinkage> links;

    Set<String> linkable = new HashSet<>();

    @PostConstruct
    protected void buildLinkNameSet() {
        for (ArchaiusLinkage link : this.links) {
            this.linkable.add(link.name() + ".");
        }
    }

    @Override
    public void customize() {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();

        Set<ConfigKey> seen = new HashSet<>();

        this.configurableManager.configurables().stream()
                .filter(this::shouldLink)
                .forEach(e -> {
                    try {
                        Object value = e.currentValue();
                        if (value != null) {
                            config.setProperty(e.key().subkey(1).name(), e.currentValue());
                        }
                        seen.add(e.key());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });


        this.configurableManager.configView().allKeysRecursively()
                .filter(this::shouldLink)
                .filter(k -> !seen.contains(k))
                .forEach(k -> {
                    Object value = this.configurableManager.configView().valueOf(k);
                    if (value != null) {
                        config.setProperty(k.subkey(1).name(), value);
                    }
                });
    }

    protected boolean shouldLink(ConfigKey key) {
        if (!key.head().name().equals("thorntail")) {
            return false;
        }
        return linkable.stream().anyMatch(e -> key.subkey(1).name().startsWith(e));
    }

    protected boolean shouldLink(ConfigurableHandle configurable) {
        return shouldLink(configurable.key());
    }

}
