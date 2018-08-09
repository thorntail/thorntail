/**
 *
 *   Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth;

import javax.servlet.ServletContext;

import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

/**
 * An extension that adds support for the MP-JWT custom authentication mechanism
 * Additionally, registers an Undertow handler that cleans up MP JWT principal
 */
public class JWTAuthMethodExtension implements ServletExtension {
    /**
     * This registers the JWTAuthMechanismFactory under the "MP-JWT" mechanism name
     *
     * @param deploymentInfo - the deployment to augment
     * @param servletContext - the ServletContext for the deployment
     */
    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        deploymentInfo.addAuthenticationMechanism("MP-JWT", new JWTAuthMechanismFactory());
        deploymentInfo.addInnerHandlerChainWrapper(MpJwtPrincipalCleanupHandler::new);
    }
}
