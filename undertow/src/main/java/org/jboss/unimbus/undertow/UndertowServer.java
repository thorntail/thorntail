package org.jboss.unimbus.undertow;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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

/**
 * @author Ken Finnigan
 */
@ApplicationScoped
public class UndertowServer {

    @Value("server.port")
    private int serverPort;

    private Undertow.Builder undertowBuilder = Undertow.builder();

    private Collection<Servlet> servlets;

    @PostConstruct
    private void setup() {
        undertowBuilder.addHttpListener(this.serverPort, "localhost");
    }

    public void addServlets(Collection<Servlet> servlets) {
        this.servlets = servlets;
    }

    public void start() throws ServletException {
        DeploymentInfo depInfo = Servlets.deployment()
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("somename")
                .addServlets(servlets.stream()
                                     .map(this::mapServletMetaData)
                                     .collect(Collectors.toSet()));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(depInfo);
        manager.deploy();

        HttpHandler handler = manager.start();
        PathHandler pathHandler = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", handler);

        undertowBuilder
                .setHandler(pathHandler)
                .build()
                .start();
    }

    private ServletInfo mapServletMetaData(Servlet servlet) {
        return Servlets.servlet(servlet.getClass().getName(), servlet.getClass()).addMapping("/" + servlet.getClass().getSimpleName().toLowerCase());
    }
}
