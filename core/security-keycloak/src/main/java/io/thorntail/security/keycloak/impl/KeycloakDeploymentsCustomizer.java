package io.thorntail.security.keycloak.impl;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.Deployments;

import static io.thorntail.Info.ROOT_PACKAGE;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
@RequiredClassPresent(ROOT_PACKAGE + ".servlet.Deployments")
public class KeycloakDeploymentsCustomizer {

    void customize(@Observes @Priority(100) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    void customize(DeploymentMetaData deployment) {
        if (deployment.getAuthMethods().contains("KEYCLOAK")) {
            deployment.addInitParam("keycloak.config.resolver", ConfigResolver.class.getName());
            deployment.setRealm("");
        }
    }

    @Inject
    Deployments deployments;


}
