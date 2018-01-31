package org.jboss.unimbus.config.impl;

class SystemEnvironmentConfigSource extends MapConfigSource {

    SystemEnvironmentConfigSource() {
        super("system environment", System.getenv());
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
