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
 *
 */

package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.lang.annotation.Annotation;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class CustomClaimProducer {
    @Claim("")
    @Dependent
    @Produces
    Object genericClaimProducer(InjectionPoint injectionPoint) {
        JsonWebToken jwt = MPJWTProducer.getJWTPrincpal();
        String name = getName(injectionPoint);
        if (name == null || name.isEmpty() || jwt == null) {
            return null;
        }
        Object value = jwt.getClaim(name);
        return value;
    }

    @Claim("")
    @Dependent
    @Produces
    Optional genericOptionalClaimProducer(InjectionPoint injectionPoint) {
        JsonWebToken jwt = MPJWTProducer.getJWTPrincpal();
        String name = getName(injectionPoint);
        if (name == null || name.isEmpty() || jwt == null) {
            return null;
        }
        Object value = jwt.getClaim(name);
        return Optional.ofNullable(value);
    }

    private String getName(InjectionPoint injectionPoint) {
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(Claim.class)) {
                // Check for a non-default value
                Claim claim = (Claim) qualifier;
                String name = claim.standard() == Claims.UNKNOWN ? claim.value() : claim.standard().name();
                if (name.length() == 0) {
                    //
                    name = injectionPoint.getMember().getName();
                }
                return name;
            }
        }
        return null;
    }
}
