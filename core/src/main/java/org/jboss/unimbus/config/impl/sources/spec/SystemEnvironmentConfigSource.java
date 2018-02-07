package org.jboss.unimbus.config.impl.sources.spec;

import org.jboss.unimbus.config.impl.sources.ConfigSources;
import org.jboss.unimbus.config.impl.sources.MapConfigSource;

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
