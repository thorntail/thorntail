package org.jboss.unimbus.config;

import java.util.Properties;

public class PropertiesConfiguration implements Configuration {

    public PropertiesConfiguration(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Object get(String key) {
        return this.properties.get(key);
    }

    private final Properties properties;
}
