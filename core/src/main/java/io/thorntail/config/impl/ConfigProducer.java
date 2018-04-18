package io.thorntail.config.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class ConfigProducer {

    @Produces
    @ApplicationScoped
    Config config() {
        return ConfigProvider.getConfig();
    }

}
