package org.jboss.unimbus.undertow;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.servlet.spi.WebServer;

/**
 * @author Ken Finnigan
 */
//@ApplicationScoped
    @Vetoed
public class UndertowServer implements WebServer {

    @Inject
    @ConfigProperty(name="web.server.port")
    private int serverPort;

    @Inject
    private HttpHandler root;

    private Undertow.Builder undertowBuilder = Undertow.builder();

    private Undertow undertow;

    @PostConstruct
    private void setup() {
        System.err.println( "SETTING PORT: " + this.serverPort);
        undertowBuilder.addHttpListener(this.serverPort, "localhost");
    }

    public void start() {
        System.err.println( "STARTING " + this.root );
        undertow = undertowBuilder
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        System.err.println( "exchange: " + exchange );
                        root.handleRequest(exchange);
                    }
                })
                .build();
        undertow.start();
    }

    @Override
    public void stop() {
        if (undertow != null) {
            undertow.stop();
        }
    }

    public Undertow getUndertow() {
        return this.undertow;
    }
}
