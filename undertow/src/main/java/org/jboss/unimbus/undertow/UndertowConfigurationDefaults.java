package org.jboss.unimbus.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.unimbus.config.DefaultValue;

@ApplicationScoped
public class UndertowConfigurationDefaults {

    @Produces
    @DefaultValue("server.port")
    private int serverPort = 8080;
}
