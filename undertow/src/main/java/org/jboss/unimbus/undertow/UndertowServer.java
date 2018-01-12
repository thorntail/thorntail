package org.jboss.unimbus.undertow;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.unimbus.config.Value;
import org.jboss.unimbus.servlet.spi.WebServer;

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class UndertowServer implements WebServer {

    @Value("server.port")
    private int serverPort;

    private Undertow.Builder undertowBuilder = Undertow.builder();

    @Any
    @Inject
    private Instance<Servlet> servlets;

    private Undertow undertow;

    @PostConstruct
    private void setup() {
        undertowBuilder.addHttpListener(8080, "localhost");
    }

    public void start() {
        DeploymentInfo depInfo = Servlets.deployment()
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("somename")
                .addServlets(servlets.stream()
                                     .map(this::mapServletMetaData)
                                     .collect(Collectors.toSet()));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(depInfo);
        manager.deploy();

        HttpHandler handler = null;
        try {
            handler = manager.start();
        } catch (ServletException e) {
            //TODO Proper logging
            e.printStackTrace();
        }
        PathHandler pathHandler = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", handler);

        undertow = undertowBuilder
                .setHandler(pathHandler)
                .build();

        undertow.start();
    }

    @Override
    public void stop() {
        if (undertow != null) {
            undertow.stop();
        }
    }

    private ServletInfo mapServletMetaData(Servlet servlet) {
        return Servlets.servlet(servlet.getClass().getName(), servlet.getClass()).addMapping("/" + servlet.getClass().getSimpleName().toLowerCase());
    }
}
