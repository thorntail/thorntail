package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class JWTProducer {

    /**
     * The @RequestScoped producer method for the current JsonWebToken
     *
     * @return
     */
    @Produces
    @Dependent
    JsonWebToken currentJWTPrincipalOrNull() {
        return MPJWTProducer.getJWTPrincpal();
    }
}
