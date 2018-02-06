package org.jboss.unimbus.config.impl;

class FrameworkDefaultsPropertiesConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    FrameworkDefaultsPropertiesConfigSourceProvider() {
        super("META-INF/framework-defaults.properties", -1000);
    }
}
