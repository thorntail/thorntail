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
import org.jboss.unimbus.servlet.Primary;
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
            Undertow undertow = configure(builder, new AnnotationLiteral<Primary>() { });
            this.primaryUndertow = undertow;
            this.managementUndertow = undertow;
        } else {
            if ( this.selector.isPrimaryEnabled() ) {
                Undertow.Builder builder = Undertow.builder();
                builder.setHandler(this.primaryRoot);
                this.primaryUndertow = configure(builder, new AnnotationLiteral<Primary>() { });
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
        if ( this.selector.isUnified() ) {
            this.primaryUndertow.start();
        } else {
            if ( selector.isPrimaryEnabled() ) {
                this.primaryUndertow.start();
            }
            if ( selector.isManagementEnabled() ) {
                this.managementUndertow.start();
            }
        }
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
