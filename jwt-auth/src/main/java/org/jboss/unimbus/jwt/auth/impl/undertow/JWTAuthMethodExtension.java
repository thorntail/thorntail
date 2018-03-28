package org.jboss.unimbus.jwt.auth.impl.undertow;

import javax.servlet.ServletContext;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

public class JWTAuthMethodExtension implements ServletExtension {
    /**
     * This registers the JWTAuthMechanismFactory under the "MP-JWT" mechanism name
     *
     * @param deploymentInfo - the deployment to augment
     * @param servletContext - the ServletContext for the deployment
     */
    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        System.err.println( "HANDLE DEPLOYMENT: " + deploymentInfo.isAuthenticationMechanismPresent("MP-JWT"));

        deploymentInfo.addAuthenticationMechanism("MP-JWT", new JWTAuthMechanismFactory());
    }
}
