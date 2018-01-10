package org.jboss.unimbus.example;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

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
        try {
            undertow.addServlets(servlets.stream().collect(Collectors.toSet()));
            undertow.start();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
