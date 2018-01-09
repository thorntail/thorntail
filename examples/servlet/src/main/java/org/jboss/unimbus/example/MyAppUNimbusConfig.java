package org.jboss.unimbus.example;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;

import io.undertow.Undertow;
import org.jboss.unimbus.spi.UNimbusConfiguration;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class MyAppUNimbusConfig implements UNimbusConfiguration {
//    @Inject
//    Undertow undertow;

    @Inject
    @Any
    Instance<Servlet> servlets;

    @Override
    public void run() {
//        undertow.start();
    }
}
