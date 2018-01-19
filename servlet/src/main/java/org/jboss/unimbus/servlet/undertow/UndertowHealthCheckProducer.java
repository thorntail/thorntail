package org.jboss.unimbus.servlet.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.Undertow;
import org.eclipse.microprofile.health.Health;
import org.jboss.unimbus.servlet.Management;
import org.jboss.unimbus.servlet.Primary;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class UndertowHealthCheckProducer {

    @Produces
    @Health
    @Primary
    UndertowHealthCheck primaryHealthCheck() {
        if (selector.isUnified()) {
            return new UndertowHealthCheck("undertow", this.primaryUndertow);
        }

        if (selector.isPrimaryEnabled()) {
            return new UndertowHealthCheck("undertow-primary", this.primaryUndertow);
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
    @Primary
    Undertow primaryUndertow;

    @Inject
    @Management
    Undertow managementUndertow;

}
