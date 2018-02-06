package org.jboss.unimbus.config.impl;

class ApplicationProfilePropertiesConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    ApplicationProfilePropertiesConfigSourceProvider(String profileName) {
        super("META-INF/application-" + profileName + ".properties", 500);
    }
}
