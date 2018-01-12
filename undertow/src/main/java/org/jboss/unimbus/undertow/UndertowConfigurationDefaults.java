package org.jboss.unimbus.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class UndertowConfigurationDefaults {

    @Produces
    private int serverPort = 8080;
}
