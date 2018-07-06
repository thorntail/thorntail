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

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OIDCHttpFacade;

public class KeycloakMultitenancyConfigResolver implements KeycloakConfigResolver {

    private Map<String, KeycloakDeployment> pathDeployments;
    public KeycloakMultitenancyConfigResolver(Map<String, KeycloakDeployment> pathDeployments) {
        this.pathDeployments = pathDeployments;
    }

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        // Select the deployment using the relative request path
        String path = request.getRelativePath();

        // Try to get the exact match first
        Optional<KeycloakDeployment> dep = Optional.ofNullable(pathDeployments.get(path));

        // If no exact match exists then iterate over the pathDeployments entries
        // and find the first deployment whose entry path is a prefix of the request path
        return dep.orElse(getMatchingPathDeployment(path).orElseThrow(throwException(path)));
    }

    private Optional<KeycloakDeployment> getMatchingPathDeployment(String path) {
        return pathDeployments.entrySet().stream().filter(e -> path.startsWith(e.getKey()))
            .findFirst().map(e -> e.getValue());
    }

    private static Supplier<IllegalStateException> throwException(String path) {
        return () -> new IllegalStateException("No Keycloak configuration for the path " + path);
    }
}