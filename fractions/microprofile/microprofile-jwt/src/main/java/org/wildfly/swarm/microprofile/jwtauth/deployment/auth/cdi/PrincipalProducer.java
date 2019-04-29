package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Override the default CDI Principal bean to allow the injection of a Principal to be a JsonWebToken
 */
@Priority(1)
@Alternative
@RequestScoped
public class PrincipalProducer {
    private JsonWebToken token;

    public PrincipalProducer() {
    }

    public void setJsonWebToken(JsonWebToken token) {
        this.token = token;
    }

    /**
     * The producer method for the current JsonWebToken
     *
     * @return JsonWebToken
     */
    @Produces
    @RequestScoped
    JsonWebToken currentJWTPrincipalOrNull() {
        return token == null ? new NullJsonWebToken() : token;
    }

    private static class NullJsonWebToken implements JsonWebToken {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<String> getClaimNames() {
            return null;
        }

        @Override
        public <T> T getClaim(String claimName) {
            return null;
        }
    }
}