package org.jboss.unimbus.security.keycloak;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.unimbus.condition.IfClassPresent;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
@IfClassPresent("org.jboss.unimbus.servlet.Deployments")
public class KeycloakDeploymentsCustomizer {

    void customize(@Observes @Priority(100) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    void customize(DeploymentMetaData deployment) {
        System.err.println( "check " + deployment.getName() + " // " + deployment.getAuthMethods() );
        if ( deployment.getAuthMethods().contains("KEYCLOAK")) {
            deployment.addInitParam( "keycloak.config.resolver", ConfigResolver.class.getName());
            deployment.setRealm("");
        }
    }

    @Inject
    Deployments deployments;


}
