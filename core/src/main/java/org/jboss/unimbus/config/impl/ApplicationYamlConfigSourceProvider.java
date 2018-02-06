package org.jboss.unimbus.config.impl;

class ApplicationYamlConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    ApplicationYamlConfigSourceProvider() {
        super("META-INF/application.yaml", 200);
    }

}
