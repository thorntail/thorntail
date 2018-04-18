package io.thorntail.servlet.impl.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class UndertowSelector {


    public boolean isManagementEnabled() {
        return (this.managementServerPort >= 0);
    }

    public boolean isPrimaryEnabled() {
        return (this.primaryServerPort >= 0);
    }

    public boolean isUnified() {
        if (!isManagementEnabled() || !isPrimaryEnabled()) {
            return false;
        }
        return ((this.managementServerHost.equals(this.primaryServerHost))
                &&
                (this.managementServerPort == this.primaryServerPort));
    }

    @Inject
    @ConfigProperty(name = "web.primary.port")
    int primaryServerPort;

    @Inject
    @ConfigProperty(name = "web.primary.host")
    String primaryServerHost;

    @Inject
    @ConfigProperty(name = "web.management.port")
    int managementServerPort;

    @Inject
    @ConfigProperty(name = "web.management.host")
    String managementServerHost;
}
