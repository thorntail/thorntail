/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.thorntail.security.keycloak.impl;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OIDCHttpFacade;

public class StaticKeycloakConfigResolver implements KeycloakConfigResolver {

    private KeycloakDeployment deployment;
    public StaticKeycloakConfigResolver(KeycloakDeployment deployment) {
        this.deployment = deployment;
    }
    
    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        return deployment;
    }
}