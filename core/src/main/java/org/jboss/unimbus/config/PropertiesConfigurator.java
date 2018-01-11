package org.jboss.unimbus.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PropertiesConfigurator {

    @Produces
    @ApplicationScoped
    Configuration systemProperties() {
        return new PropertiesConfiguration(System.getProperties());
    }
}
