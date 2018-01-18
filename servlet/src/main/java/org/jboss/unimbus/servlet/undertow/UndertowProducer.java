package org.jboss.unimbus.servlet.undertow;

import java.lang.annotation.Annotation;

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
import org.jboss.unimbus.servlet.Public;
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
            builder.setHandler(this.publicRoot);
            Undertow undertow = configure(builder, new AnnotationLiteral<Public>() { });
            this.publicUndertow = undertow;
            this.managementUndertow = undertow;
        } else {
            if ( this.selector.isPublicEnabled() ) {
                Undertow.Builder builder = Undertow.builder();
                builder.setHandler(this.publicRoot);
                this.publicUndertow = configure(builder, new AnnotationLiteral<Public>() { });
            }
            if ( this.selector.isManagementEnabled() ) {
                Undertow.Builder builder = Undertow.builder();
                builder.setHandler(this.managementRoot);
                this.managementUndertow = configure(builder, new AnnotationLiteral<Management>() { });
            }
        }
    }

    private Undertow configure(Undertow.Builder builder, Annotation annotation) {

        this.configurers.select(annotation)
                .forEach( config->{
                    config.configure(builder);
                });

        return builder.build();
    }

    @Produces
    @Public
    Undertow publicUndertow() {
        return this.publicUndertow;
    }

    @Produces
    @Management
    Undertow managementUndertow() {
        return this.managementUndertow;
    }

    void start(@Observes LifecycleEvent.Start event) {
        if ( this.selector.isUnified() ) {
            this.publicUndertow.start();
        } else {
            if ( selector.isPublicEnabled() ) {
                this.publicUndertow.start();
            }
            if ( selector.isManagementEnabled() ) {
                this.managementUndertow.start();
            }
        }
    }

    @Inject
    UndertowSelector selector;

    @Inject
    @Public
    PathHandler publicRoot;

    @Inject
    @Management
    PathHandler managementRoot;

    private Undertow publicUndertow;
    private Undertow managementUndertow;

    @Inject
    @Any
    private Instance<UndertowConfigurer> configurers;
}
