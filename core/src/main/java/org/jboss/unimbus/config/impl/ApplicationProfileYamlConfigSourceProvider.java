package org.jboss.unimbus.config.impl;

class ApplicationProfileYamlConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    ApplicationProfileYamlConfigSourceProvider(String profileName) {
        super("META-INF/application-" + profileName + ".yaml", 500);
    }

}
