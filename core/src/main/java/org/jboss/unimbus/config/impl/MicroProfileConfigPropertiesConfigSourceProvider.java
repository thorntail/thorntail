package org.jboss.unimbus.config.impl;

class MicroProfileConfigPropertiesConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    MicroProfileConfigPropertiesConfigSourceProvider() {
        super("META-INF/microprofile-config.properties", 100);
    }
}
