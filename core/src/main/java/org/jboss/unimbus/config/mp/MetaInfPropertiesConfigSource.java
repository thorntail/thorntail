package org.jboss.unimbus.config.mp;

class MetaInfPropertiesConfigSource extends ClasspathResourcesConfigSource {

    MetaInfPropertiesConfigSource(ClassLoader classLoader) {
        super("META-INF/microprofile-config.properties", classLoader);
    }
}
