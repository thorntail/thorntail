package org.jboss.unimbus.jwt.auth.impl;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;
import org.jboss.unimbus.util.Annotations;

/**
 * Created by bob on 3/27/18.
 */
@ApplicationScoped
public class LoginConfigDeploymentsCustomizer {
    void customize(@Observes @Priority(100) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    void customize(DeploymentMetaData deployment) {
        Application app = deployment.getAttachment(Application.class);
        if (app != null) {
            LoginConfig loginConfig = Annotations.getAnnotation(app, LoginConfig.class);
            if ( loginConfig != null ) {
                System.err.println("--> " + loginConfig);
                deployment.addAuthMethod(loginConfig.authMethod());
                deployment.setRealm(loginConfig.realmName());
            }
        }
        /*
        if ( deployment.getAuthMethods().contains("KEYCLOAK")) {
            deployment.addInitParam( "keycloak.config.resolver", ConfigResolver.class.getName());
            deployment.setRealm("");
        }
        */
    }

    @Inject
    Deployments deployments;
}
