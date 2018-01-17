package org.jboss.unimbus.servlet.undertow;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class UndertowProducer {

    @PostConstruct
    void init() {
        Undertow.Builder builder = Undertow.builder();
        builder.addHttpListener(this.serverPort, this.serverHost);
        builder.setHandler(this.root);
        this.undertow = builder.build();
    }

    @Produces
    Undertow undertow() {
        return this.undertow;
    }

    void start(@Observes LifecycleEvent.Start event) {
        System.err.println( "Starting undertow on http://" + this.serverHost + ":" + this.serverPort );
        this.undertow.start();
    }

    @Inject
    @ConfigProperty(name="web.server.port")
    private int serverPort;

    @Inject
    @ConfigProperty(name="web.server.host")
    private String serverHost;

    @Inject
    private HttpHandler root;

    private Undertow undertow;
}
