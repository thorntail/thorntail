package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.wildfly.swarm.microprofile.jwtauth.deployment.auth.JWTAccount;

/**
 * Override the default CDI Principal bean to allow the injection of a Principal to be a JsonWebToken
 */
@Priority(1)
@Alternative
@RequestScoped
public class PrincipalProducer {
    private JWTAccount account;

    public PrincipalProducer() {
    }

    public JWTAccount getAccount() {
        return account;
    }

    public void setAccount(JWTAccount account) {
        this.account = account;
    }

    /**
     * The producer method for the current JsonWebToken
     *
     * @return
     */
    @Produces
    @RequestScoped
    JsonWebToken currentJWTPrincipalOrNull() {
        JsonWebToken token = null;
        if (account != null) {
            token = (JsonWebToken) account.getPrincipal();
        }
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