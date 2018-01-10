package org.jboss.unimbus.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;

import org.jboss.unimbus.spi.UNimbusConfiguration;
import org.jboss.unimbus.undertow.UndertowServer;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class MyAppUNimbusConfig implements UNimbusConfiguration {

    @Inject
    UndertowServer undertow;

    @Inject
    @Any
    Instance<Servlet> servlets;

    @Override
    public void run() {
        undertow.start();
    }
}
