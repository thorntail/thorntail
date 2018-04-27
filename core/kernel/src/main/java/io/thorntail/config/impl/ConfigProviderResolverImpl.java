package io.thorntail.config.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class ConfigProviderResolverImpl extends ConfigProviderResolver {

    public ConfigProviderResolverImpl() {

    }

    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        Config config = this.configs.get(loader);
        if (config == null) {
            config = getBuilder()
                    .addDefaultSources()
                    .addDiscoveredConverters()
                    .addDiscoveredSources()
                    .build();
            registerConfig(config, loader);
        }
        return config;
    }

    @Override
    public ConfigBuilder getBuilder() {
        return new ConfigBuilderImpl();
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        this.configs.put(classLoader, config);
    }

    @Override
    public void releaseConfig(Config config) {
        Set<ClassLoader> keys = this.configs.entrySet().stream()
                .filter(e -> e.getValue().equals(config))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());

        keys.forEach(e -> this.configs.remove(e));
    }

    private Map<ClassLoader, Config> configs = new HashMap<>();
}
