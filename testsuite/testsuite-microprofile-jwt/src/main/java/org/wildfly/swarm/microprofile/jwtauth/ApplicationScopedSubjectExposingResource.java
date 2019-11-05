/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth;

import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("subject")
@ApplicationScoped
public class ApplicationScopedSubjectExposingResource {

    @Inject
    private JsonWebToken token;

    @Inject
    @Claim(standard = Claims.sub)
    private ClaimValue<String> sub;

    @Inject
    @Claim(standard = Claims.sub)
    private ClaimValue<Optional<String>> optionalSub;

    @Inject
    @Claim(standard = Claims.sub)
    private Provider<String> providerSub;

    @Inject
    @Claim(standard = Claims.sub)
    private Provider<Optional<String>> providerOptionalSub;

    @GET
    @RolesAllowed("MappedRole")
    @Path("secured")
    public String getSubjectSecured() {
        return token.getSubject();
    }

    @GET
    @RolesAllowed("MappedRole")
    @Path("secured/claim-value")
    public String getSubjectSecuredClaimValue() {
        return sub.getValue();
    }

    @GET
    @RolesAllowed("MappedRole")
    @Path("secured/claim-value-optional")
    public String getSubjectSecuredClaimValueOptional() {
        return optionalSub.getValue().get();
    }

    @GET
    @RolesAllowed("MappedRole")
    @Path("secured/provider")
    public String getSubjectSecuredProvider() {
        return providerSub.get();
    }

    @GET
    @RolesAllowed("MappedRole")
    @Path("secured/provider-optional")
    public String getSubjectSecuredProviderOptional() {
        return providerOptionalSub.get().get();
    }

    @GET
    @Path("unsecured")
    @PermitAll
    public String getSubjectUnsecured() {
        return token != null ? token.getSubject() : null;
    }

}
