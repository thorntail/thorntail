package io.thorntail.jwt.auth.impl;

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;

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
        JWTCallerPrincipal principal = null;

        try {
            JwtConsumerBuilder builder = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setRequireSubject()
                    .setSkipDefaultAudienceValidation()
                    .setExpectedIssuer(authContextInfo.getIssuedBy())
                    .setVerificationKey(authContextInfo.getSignerKey())
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                                     AlgorithmIdentifiers.RSA_USING_SHA256));
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

