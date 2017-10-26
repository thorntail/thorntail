/*
 * Copyright (c) 2016-2017 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.wildfly.swarm.mpjwtauth.deployment.principal;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * An abstract CallerPrincipal implementation that provides access to the JWT claims that are required by
 * the microprofile token.
 */
public abstract class JWTCallerPrincipal implements JsonWebToken {
    private String name;

    /**
     * Create a JWTCallerPrincipal with the caller's name
     *
     * @param name - caller's name
     */
    public JWTCallerPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Generate a human readable version of the caller principal and associated JWT.
     *
     * @param showAll - should all claims associated with the JWT be displayed or should only those defined in the
     *                JsonWebToken interface be displayed.
     * @return human readable presentation of the caller principal and associated JWT.
     */
    public abstract String toString(boolean showAll);

    public <T> Optional<T> claim(String claimName) {
        T claim = (T) getClaim(claimName);
        return Optional.ofNullable(claim);
    }
}
