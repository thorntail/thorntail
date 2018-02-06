package org.jboss.unimbus.config.impl;

class FrameworkDefaultsYamlConfigSourceProvider extends ClasspathResourcesConfigSourceProvider {

    FrameworkDefaultsYamlConfigSourceProvider() {
        super("META-INF/framework-defaults.yaml", -1000);
    }

}
