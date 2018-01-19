package org.jboss.unimbus.security.basic;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.security.SecurityMessages;

/**
 * Created by bob on 1/19/18.
 */
@ApplicationScoped
@Priority(0)
public class BasicSecurityInitializer {

    void initialize(@Observes LifecycleEvent.Initialize event) {
        if ( ! this.managementSecurity.isPresent() ) {
            return;
        }

        if ( this.managementSecurity.get().equals("basic")) {
            BasicSecurity security = this.security.get();
            if ( security.isEmpty() ) {
                String tempPassword = UUID.randomUUID().toString();
                User user = security.addUser("admin", tempPassword);
                user.addRole( "admin" );
                SecurityMessages.MESSAGES.temporaryAdminPassword(tempPassword);
            }
        }
    }

    @Inject
    @ConfigProperty(name="web.management.security")
    Optional<String> managementSecurity;

    @Inject
    Instance<BasicSecurity> security;
}
