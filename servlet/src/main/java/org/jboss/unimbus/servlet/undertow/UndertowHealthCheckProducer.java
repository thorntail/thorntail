package org.jboss.unimbus.servlet.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.health.Health;
import org.jboss.unimbus.servlet.Management;
import org.jboss.unimbus.servlet.Public;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class UndertowHealthCheckProducer {

    @Produces
    @Health
    @Public
    UndertowHealthCheck publicHealthCheck() {
        if (selector.isUnified()) {
            return new UndertowHealthCheck("undertow", this.publicUndertow);
        }

        if (selector.isPublicEnabled()) {
            return new UndertowHealthCheck("undertow-public", this.publicUndertow);
        }

        return null;
    }

    @Produces
    @Health
    @Management
    UndertowHealthCheck managementHealthCheck() {
        if (selector.isUnified()) {
            return null;
        }
        if (selector.isManagementEnabled()) {
            return new UndertowHealthCheck("undertow-management", this.managementUndertow);
        }

        return null;
    }

    @Inject
    UndertowSelector selector;

    @Inject
    @Public
    Undertow publicUndertow;

    @Inject
    @Management
    Undertow managementUndertow;

}
