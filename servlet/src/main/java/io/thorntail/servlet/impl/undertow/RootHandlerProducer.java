package io.thorntail.servlet.impl.undertow;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.server.handlers.PathHandler;
import io.thorntail.servlet.annotation.Management;
import io.thorntail.servlet.annotation.Primary;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class RootHandlerProducer {

    @PostConstruct
    void init() {
        if (this.selector.isUnified()) {
            this.primaryRoot = new PathHandler();
            this.managementRoot = this.primaryRoot;
        } else {
            if (this.selector.isPrimaryEnabled()) {
                this.primaryRoot = new PathHandler();
            }
            if (this.selector.isManagementEnabled()) {
                this.managementRoot = new PathHandler();
            }
        }
    }

    @Produces
    @Primary
    PathHandler primaryRoot() {
        return this.primaryRoot;
    }

    @Produces
    @Management
    PathHandler managementRoot() {
        return this.managementRoot;
    }

    @Inject
    private UndertowSelector selector;

    private PathHandler primaryRoot;

    private PathHandler managementRoot;

}
