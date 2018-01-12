package org.jboss.unimbus.config.mp;

class UNimbusDefaultsConfigSource extends ClasspathResourcesConfigSource {

    UNimbusDefaultsConfigSource(ClassLoader classLoader) {
        super("META-INF/unimbus-defaults.properties", classLoader);
    }

    @Override
    public int getOrdinal() {
        return -10000;
    }
}
