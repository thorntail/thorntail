package org.wildfly.swarm.keycloak.mpjwt.deployment;

import java.io.InputStream;

import org.jboss.logging.Logger;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.representations.AccessToken;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;

/**
 * An implementation of the abstract JWTCallerPrincipalFactory that uses the Keycloak token parsing classes.
 */
public class KeycloakJWTCallerPrincipalFactory extends JWTCallerPrincipalFactory {
    private static final Logger log = Logger.getLogger(KeycloakJWTCallerPrincipalFactory.class);

    private static final KeycloakDeployment deployment = createDeployment();

    @Override
    public JWTCallerPrincipal parse(final String token, final JWTAuthContextInfo authContextInfo) throws ParseException {
        try {
            JWSInput jwsInput = new JWSInput(token);
            AccessToken accessToken = AdapterTokenVerifier.verifyToken(jwsInput.getWireString(), deployment);
            return new KeycloakJWTCallerPrincipal(jwsInput.readContentAsString(), accessToken);
        } catch (Throwable ex) {
            throw new ParseException("Failure to parse the token", ex);
        }
    }

    private static KeycloakDeployment createDeployment() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream keycloakJson = cl.getResourceAsStream("keycloak.json");
        if (keycloakJson == null) {
            keycloakJson = cl.getResourceAsStream("WEB-INF/keycloak.json");
        }
        if (keycloakJson == null) {
            log.warn("keycloak.json resource is not available");
            return null;
        } else {
            return KeycloakDeploymentBuilder.build(keycloakJson);
        }
    }
}
