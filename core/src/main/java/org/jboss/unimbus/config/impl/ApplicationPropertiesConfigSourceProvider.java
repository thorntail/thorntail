package org.jboss.unimbus.config.impl;

class ApplicationPropertiesConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    ApplicationPropertiesConfigSourceProvider() {
        super("META-INF/application.properties", 200);
    }
}
