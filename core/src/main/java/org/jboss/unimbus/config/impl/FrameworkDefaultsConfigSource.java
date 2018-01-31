package org.jboss.unimbus.config.impl;

class FrameworkDefaultsConfigSource extends ClasspathResourcesConfigSource {

    FrameworkDefaultsConfigSource(ClassLoader classLoader) {
        super("META-INF/framework-defaults.properties", classLoader);
    }

    @Override
    public int getOrdinal() {
        return -10000;
    }
}
