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
package org.wildfly.swarm.microprofile.jwtauth.deployment.principal;

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;

import java.util.List;

/**
 * A default implementation of the abstract JWTCallerPrincipalFactory that uses the Keycloak token parsing classes.
 */
public class DefaultJWTCallerPrincipalFactory extends JWTCallerPrincipalFactory {

    /**
     * Tries to load the JWTAuthContextInfo from CDI if the class level authContextInfo has not been set.
     */
    public DefaultJWTCallerPrincipalFactory() {
    }

    @Override
    public JWTCallerPrincipal parse(final String token, final JWTAuthContextInfo authContextInfo) throws ParseException {
        JWTCallerPrincipal principal;

        try {
            JwtConsumerBuilder builder = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setRequireSubject()
                    .setSkipDefaultAudienceValidation()
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                    AlgorithmIdentifiers.RSA_USING_SHA256));

            if (authContextInfo.isRequireIssuer()) {
                builder.setExpectedIssuer(true, authContextInfo.getIssuedBy());
            } else {
                builder.setExpectedIssuer(false, null);
            }
            if (authContextInfo.getSignerKey() != null) {
                builder.setVerificationKey(authContextInfo.getSignerKey());
            } else if (authContextInfo.isFollowMpJwt11Rules()) {
                builder.setVerificationKeyResolver(new KeyLocationResolver(authContextInfo.getJwksUri()));
            } else {
                final List<JsonWebKey> jsonWebKeys = authContextInfo.loadJsonWebKeys();
                builder.setVerificationKeyResolver(new JwksVerificationKeyResolver(jsonWebKeys));
            }

            if (authContextInfo.getExpGracePeriodSecs() > 0) {
                builder.setAllowedClockSkewInSeconds(authContextInfo.getExpGracePeriodSecs());
            } else {
                builder.setEvaluationTime(NumericDate.fromSeconds(0));
            }

            JwtConsumer jwtConsumer = builder.build();
            JwtContext jwtContext = jwtConsumer.process(token);
            String type = jwtContext.getJoseObjects().get(0).getHeader("typ");
            //  Validate the JWT and process it to the Claims
            jwtConsumer.processContext(jwtContext);
            JwtClaims claimsSet = jwtContext.getJwtClaims();

            // We have to determine the unique name to use as the principal name. It comes from upn, preferred_username, sub in that order
            String principalName = claimsSet.getClaimValue("upn", String.class);
            if (principalName == null) {
                principalName = claimsSet.getClaimValue("preferred_username", String.class);
                if (principalName == null) {
                    principalName = claimsSet.getSubject();
                }
            }
            claimsSet.setClaim(Claims.raw_token.name(), token);
            principal = new DefaultJWTCallerPrincipal(token, type, claimsSet, principalName);
        } catch (InvalidJwtException e) {
            throw new ParseException("Failed to verify token", e);
        } catch (MalformedClaimException e) {
            throw new ParseException("Failed to verify token claims", e);
        }

        return principal;
    }
}
