package org.jboss.unimbus.servlet.undertow;

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

    public boolean isPublicEnabled() {
        return (this.publicServerPort >= 0);
    }

    public boolean isUnified() {
        if ( ! isManagementEnabled() || ! isPublicEnabled() ) {
            return false;
        }
        return ((this.managementServerHost.equals(this.publicServerHost))
                &&
                (this.managementServerPort == this.publicServerPort));
    }

    @Inject
    @ConfigProperty(name = "web.public.port")
    int publicServerPort;

    @Inject
    @ConfigProperty(name = "web.public.host")
    String publicServerHost;

    @Inject
    @ConfigProperty(name = "web.management.port")
    int managementServerPort;

    @Inject
    @ConfigProperty(name = "web.management.host")
    String managementServerHost;
}
