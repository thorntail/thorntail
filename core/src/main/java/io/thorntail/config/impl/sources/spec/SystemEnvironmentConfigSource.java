package io.thorntail.config.impl.sources.spec;

import io.thorntail.config.impl.sources.MapConfigSource;
import io.thorntail.config.impl.sources.ConfigSources;

public class SystemEnvironmentConfigSource extends MapConfigSource {

    public SystemEnvironmentConfigSource() {
        super("system environment", System.getenv());
    }

    @Override
    public int getOrdinal() {
        return ConfigSources.ENVIRONMENT_VARIABLES_ORDINAL;
    }

    @Override
    public String getValue(String propertyName) {
        String value = super.getValue(propertyName);
        if (value != null) {
            return value;
        }
        return super.getValue(environmentStyle(propertyName));
    }

    protected String environmentStyle(String propertyName) {
        propertyName = propertyName.toUpperCase();
        propertyName = propertyName.replace('.', '_');
        return propertyName;
    }
}
