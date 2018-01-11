package org.jboss.unimbus.example;

import java.sql.Driver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    Driver driver;

    @Inject
    EntityManager entityManager;


    @Inject
    @Any
    Instance<Servlet> servlets;

    @Override
    public void run() {
        /*
        try {
            undertow.addServlets(servlets.stream().collect(Collectors.toSet()));
            undertow.start();
        } catch (ServletException e) {
            e.printStackTrace();
        }
        */
        try {
            System.err.println("context: " + new InitialContext());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.err.println( "undertow: " + undertow );
        System.err.println( "driver: " + driver );
        System.err.println( "entityManager: " + entityManager);
    }
}
