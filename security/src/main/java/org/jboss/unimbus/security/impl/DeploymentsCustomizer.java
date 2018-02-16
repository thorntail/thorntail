package org.jboss.unimbus.security.impl;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;
import org.jboss.unimbus.servlet.EmptyRoleSemantic;
import org.jboss.unimbus.servlet.HttpConstraintMetaData;
import org.jboss.unimbus.servlet.SecurityConstraintMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;
import org.jboss.unimbus.servlet.ServletSecurityMetaData;
import org.jboss.unimbus.servlet.WebResourceCollectionMetaData;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
@RequiredClassPresent("org.jboss.unimbus.servlet.Deployments")
public class DeploymentsCustomizer {

    void customize(@Observes @Priority(1) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    void customize(DeploymentMetaData deployment) {
        if (deployment.isManagement() && this.managementSecurity.isPresent()) {
            deployment.addAuthMethod(managementSecurity.get().toUpperCase());
            deployment.addSecurityConstraint(new SecurityConstraintMetaData()
                                                     .addWebResourceCollection(
                                                             new WebResourceCollectionMetaData().addUrlPattern("/*")
                                                     )
                                                     .addRoleAllowed(this.managementRole.orElse("admin"))
            );
        } else if (!deployment.isManagement() && this.primarySecurity.isPresent()) {
            deployment.addAuthMethod(primarySecurity.get().toUpperCase());
            for (ServletMetaData each : deployment.getServlets()) {
                each.setSecurity(
                        new ServletSecurityMetaData()
                                .setHttpConstraint(new HttpConstraintMetaData()
                                                           .setEmptyRoleSemantic(EmptyRoleSemantic.PERMIT)
                                )
                );
            }
        }
    }

    @Inject
    Deployments deployments;

    @Inject
    @ConfigProperty(name = "web.primary.security")
    Optional<String> primarySecurity;

    @Inject
    @ConfigProperty(name = "web.primary.role")
    Optional<String> primaryRole;

    @Inject
    @ConfigProperty(name = "web.management.security")
    Optional<String> managementSecurity;

    @Inject
    @ConfigProperty(name = "web.management.role")
    Optional<String> managementRole;

}
