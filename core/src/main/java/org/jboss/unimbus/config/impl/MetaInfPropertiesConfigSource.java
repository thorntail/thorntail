package org.jboss.unimbus.config.impl;

class MetaInfPropertiesConfigSource extends ClasspathResourcesConfigSource {

    MetaInfPropertiesConfigSource(ClassLoader classLoader) {
        super("META-INF/microprofile-config.properties", classLoader);
    }
}
