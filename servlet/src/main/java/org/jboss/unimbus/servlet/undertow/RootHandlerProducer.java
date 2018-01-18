package org.jboss.unimbus.servlet.undertow;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.server.handlers.PathHandler;
import org.jboss.unimbus.servlet.Management;
import org.jboss.unimbus.servlet.Public;

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
        } else {
            if ( this.selector.isPublicEnabled() ) {
                this.publicRoot = new PathHandler();
            }
            if ( this.selector.isManagementEnabled() ) {
                this.managementRoot = new PathHandler();
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
