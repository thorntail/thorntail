/*
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
 * /
 */
package org.wildfly.swarm.microprofile.jwtauth.deployment.auth;

import java.security.Principal;
import java.util.Set;

import io.undertow.security.idm.Account;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Representation of the caller account using the JWTCallerPrincipal as an Undertow Account object.
 */
public class JWTAccount implements Account {
    private JsonWebToken principal;

    private Account delegate;

    public JWTAccount(JsonWebToken principal, Account delegate) {
        this.principal = principal;
        this.delegate = delegate;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public Set<String> getRoles() {
        return delegate.getRoles();
    }

}
