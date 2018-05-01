package io.thorntail.servlet.impl.undertow;

import java.lang.annotation.Annotation;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import io.thorntail.Thorntail;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowMessages;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Methods;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.annotation.Management;
import io.thorntail.servlet.annotation.Primary;
import io.thorntail.servlet.impl.ServletMessages;
import io.thorntail.servlet.impl.undertow.config.UndertowConfigurer;
import org.xnio.XnioWorker;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class UndertowProducer {

    @PostConstruct
    void init() {
        if (this.selector.isUnified()) {
            Undertow.Builder builder = Undertow.builder();
            builder.setWorker(this.xnioWorker);
            builder.setHandler(
                    wrapForErrorLogging(
                            wrapForStaticResources(this.primaryRoot)
                    )
            );
            Undertow undertow = configure(builder, new AnnotationLiteral<Primary>() {
            });
            this.primaryUndertow = undertow;
            this.managementUndertow = undertow;
        } else {
            if (this.selector.isPrimaryEnabled()) {
                Undertow.Builder builder = Undertow.builder();
                builder.setWorker(this.xnioWorker);
                builder.setHandler(
                        wrapForErrorLogging(
                                wrapForStaticResources(this.primaryRoot)
                        ));
                this.primaryUndertow = configure(builder, new AnnotationLiteral<Primary>() {
                });
            }
            if (this.selector.isManagementEnabled()) {
                Undertow.Builder builder = Undertow.builder();
                builder.setWorker(this.xnioWorker);
                builder.setHandler(
                        wrapForErrorLogging(
                                this.managementRoot)
                );
                this.managementUndertow = configure(builder, new AnnotationLiteral<Management>() {
                });
            }
        }
    }

    private HttpHandler wrapForErrorLogging(HttpHandler next) {
        return exchange -> {
            try {
                next.handleRequest(exchange);
            } catch (Throwable t) {
                ServletMessages.MESSAGES.requestException( exchange.getRequestURL(), t);
            }
        };
    }

    private HttpHandler wrapForStaticResources(HttpHandler next) {
        ResourceHandler resourceHandler = new ResourceHandler(this.resourceSupplier, next);

        return exchange -> {
            if (exchange.getRequestMethod().equals(Methods.GET) || exchange.getRequestMethod().equals(Methods.HEAD)) {
                resourceHandler.handleRequest(exchange);
            } else {
                next.handleRequest(exchange);
            }
        };
    }

    private Undertow configure(Undertow.Builder builder, Annotation annotation) {

        this.configurers.select(annotation)
                .forEach(config -> {
                    config.configure(builder);
                });

        return builder.build();
    }

    @Produces
    @Primary
    Undertow primaryUndertow() {
        return this.primaryUndertow;
    }

    @Produces
    @Management
    Undertow managementUndertow() {
        return this.managementUndertow;
    }

    @Produces
    @Primary
    InetSocketAddress primaryAddress() {
        if (this.selector.isPrimaryEnabled()) {
            for (Undertow.ListenerInfo info : this.primaryUndertow.getListenerInfo()) {
                return (InetSocketAddress) info.getAddress();
            }
        }

        return null;
    }

    @Produces
    @Management
    InetSocketAddress managementAddress() {
        if (this.selector.isManagementEnabled()) {
            for (Undertow.ListenerInfo info : this.managementUndertow.getListenerInfo()) {
                return (InetSocketAddress) info.getAddress();
            }
        }

        return primaryAddress();
    }

    @Produces
    @Primary
    URL primaryURL() throws MalformedURLException {
        if (this.selector.isPrimaryEnabled()) {
            for (Undertow.ListenerInfo info : this.primaryUndertow.getListenerInfo()) {
                return new URL(url(info));
            }
        }

        return null;
    }

    @Produces
    @Management
    URL managementURL() throws MalformedURLException {
        if (this.selector.isManagementEnabled()) {
            for (Undertow.ListenerInfo info : this.managementUndertow.getListenerInfo()) {
                return new URL(url(info));
            }
        }

        return primaryURL();
    }

    @Produces
    @Primary
    URI primaryURI() throws MalformedURLException, URISyntaxException {
        return primaryURL().toURI();
    }

    @Produces
    @Management
    URI managementURI() throws MalformedURLException, URISyntaxException {
        return managementURL().toURI();
    }

    void start(@Observes LifecycleEvent.Start event) {
        if (this.selector.isUnified()) {
            this.primaryUndertow.start();
            for (Undertow.ListenerInfo each : this.primaryUndertow.getListenerInfo()) {
                ServletMessages.MESSAGES.serverStarted("unified", url(each));
            }
        } else {
            if (selector.isPrimaryEnabled()) {
                this.primaryUndertow.start();
                for (Undertow.ListenerInfo each : this.primaryUndertow.getListenerInfo()) {
                    ServletMessages.MESSAGES.serverStarted("primary", url(each));
                }
            }
            if (selector.isManagementEnabled()) {
                this.managementUndertow.start();
                for (Undertow.ListenerInfo each : this.managementUndertow.getListenerInfo()) {
                    ServletMessages.MESSAGES.serverStarted("management", url(each));
                }
            }
        }
    }

    @PreDestroy
    void destroy() {
        this.primaryUndertow.stop();
        this.managementUndertow.stop();
        this.xnioWorker.shutdownNow();
    }

    String url(Undertow.ListenerInfo info) {
        StringBuffer str = new StringBuffer();

        str.append(info.getProtcol());
        str.append("://");
        SocketAddress addr = info.getAddress();
        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inet = (InetSocketAddress) addr;
            if (inet.getAddress() instanceof Inet6Address) {
                str.append("[");
                String hostString = inet.getHostString();
                if (hostString.equals("0:0:0:0:0:0:0:0")) {
                    hostString = "::";
                }
                str.append(hostString);
                str.append("]");

            } else if (inet.getAddress().isAnyLocalAddress()) {
                str.append("localhost");
            } else {
                str.append(inet.getHostString());
            }

            int port = inet.getPort();
            if (port != 80) {
                str.append(":");
                str.append(port);
            }
        }

        str.append("/");

        return str.toString();

    }

    @Inject
    UndertowSelector selector;

    @Inject
    @Primary
    PathHandler primaryRoot;

    @Inject
    @Management
    PathHandler managementRoot;

    private Undertow primaryUndertow;

    private Undertow managementUndertow;

    @Inject
    @Any
    private Instance<UndertowConfigurer> configurers;

    @Inject
    private InjectedResourceSupplier resourceSupplier;

    @Inject
    @io.thorntail.servlet.impl.undertow.Undertow
    XnioWorker xnioWorker;

    @Inject
    Thorntail system;

}
