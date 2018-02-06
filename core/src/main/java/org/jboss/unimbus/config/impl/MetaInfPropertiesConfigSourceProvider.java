package org.jboss.unimbus.config.impl;

class MetaInfPropertiesConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    MetaInfPropertiesConfigSourceProvider() {
        super("META-INF/microprofile-config.properties", 100);
    }
}
