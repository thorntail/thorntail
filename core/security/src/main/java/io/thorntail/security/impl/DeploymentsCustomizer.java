package io.thorntail.security.impl;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.Deployments;
import io.thorntail.servlet.EmptyRoleSemantic;
import io.thorntail.servlet.HttpConstraintMetaData;
import io.thorntail.servlet.SecurityConstraintMetaData;
import io.thorntail.servlet.ServletMetaData;
import io.thorntail.servlet.ServletSecurityMetaData;
import io.thorntail.servlet.WebResourceCollectionMetaData;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
@RequiredClassPresent(ROOT_PACKAGE + ".servlet.Deployments")
public class DeploymentsCustomizer {

    void customize(@Observes @Priority(1) LifecycleEvent.Initialize event) {
        for (DeploymentMetaData metaData : this.deployments) {
            customize(metaData);
        }
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
