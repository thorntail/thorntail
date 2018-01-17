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
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.annotations.Management;
import org.jboss.unimbus.annotations.Public;
import org.jboss.unimbus.events.LifecycleEvent;
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
        System.err.println( "-- " + this.configurers.isUnsatisfied() + " // " + this.configurers.isAmbiguous() + " // " + this.configurers.isResolvable() );
        for (UndertowConfigurer configurer : this.configurers) {
            System.err.println( "configurer register: " + configurer);
        }

        this.configurers.select(annotation)
                .forEach( config->{
                    System.err.println( "APPLY: " + config );
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
        System.err.println( "**** START UNDERTOWS" );
        if ( this.selector.isUnified() ) {
            System.err.println( "**** START UNIFIED " + this.publicUndertow );
            System.err.println("Starting undertow");
            this.publicUndertow.start();
        } else {
            if ( selector.isPublicEnabled() ) {
                System.err.println( "**** START PUBLIC " + this.publicUndertow );
                this.publicUndertow.start();
            }
            if ( selector.isManagementEnabled() ) {
                System.err.println( "**** START MANAGEMENT " + this.managementUndertow );
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
