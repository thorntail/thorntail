package org.jboss.unimbus.servlet.undertow;

import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.Management;
import org.jboss.unimbus.servlet.Primary;
import org.jboss.unimbus.servlet.ServletMessages;
import org.jboss.unimbus.servlet.undertow.config.UndertowConfigurer;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class UndertowProducer {

    @PostConstruct
    void init() {
        if (this.selector.isUnified()) {
            Undertow.Builder builder = Undertow.builder();
            builder.setHandler(this.primaryRoot);
            Undertow undertow = configure(builder, new AnnotationLiteral<Primary>() {
            });
            this.primaryUndertow = undertow;
            this.managementUndertow = undertow;
        } else {
            if (this.selector.isPrimaryEnabled()) {
                Undertow.Builder builder = Undertow.builder();
                builder.setHandler(this.primaryRoot);
                this.primaryUndertow = configure(builder, new AnnotationLiteral<Primary>() {
                });
            }
            if (this.selector.isManagementEnabled()) {
                Undertow.Builder builder = Undertow.builder();
                builder.setHandler(this.managementRoot);
                this.managementUndertow = configure(builder, new AnnotationLiteral<Management>() {
                });
            }
        }
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

    String url(Undertow.ListenerInfo info) {
        StringBuffer str = new StringBuffer();

        str.append(info.getProtcol());
        str.append("://");
        SocketAddress addr = info.getAddress();
        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inet = (InetSocketAddress) addr;
            if (inet.getAddress().isAnyLocalAddress()) {
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
}
