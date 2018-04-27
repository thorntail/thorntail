package io.thorntail.security.basic.impl;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.events.LifecycleEvent;
import io.thorntail.security.basic.User;
import io.thorntail.security.impl.SecurityMessages;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.thorntail.security.basic.BasicSecurity;

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
