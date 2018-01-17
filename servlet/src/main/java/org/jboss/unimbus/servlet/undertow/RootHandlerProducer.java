package org.jboss.unimbus.servlet.undertow;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.server.handlers.PathHandler;
import org.jboss.unimbus.annotations.Management;
import org.jboss.unimbus.annotations.Public;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class RootHandlerProducer {

    @PostConstruct
    void init() {
        if ( this.selector.isUnified() ) {
            this.publicRoot = new PathHandler();
            this.managementRoot = this.publicRoot;
            System.err.println( "create unified root " + this.publicRoot );
        } else {
            if ( this.selector.isPublicEnabled() ) {
                this.publicRoot = new PathHandler();
                System.err.println( "create public root " + this.publicRoot);
            }
            if ( this.selector.isManagementEnabled() ) {
                this.managementRoot = new PathHandler();
                System.err.println( "create management root " + this.managementRoot);
            }
        }
    }

    @Produces
    @Public
    PathHandler publicHandler() {
        return this.publicRoot;
    }

    @Produces
    @Management
    PathHandler managementHandler() {
        return this.managementRoot;
    }

    @Inject
    private UndertowSelector selector;

    private PathHandler publicRoot;
    private PathHandler managementRoot;

}
