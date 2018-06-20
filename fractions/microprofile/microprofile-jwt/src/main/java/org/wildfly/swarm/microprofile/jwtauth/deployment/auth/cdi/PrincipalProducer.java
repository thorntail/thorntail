package org.wildfly.swarm.microprofile.jwtauth.deployment.auth.cdi;

import java.security.Principal;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.jwt.JsonWebToken;

@Priority(1)
@Alternative
public class PrincipalProducer {
    /**
     * The @RequestScoped producer method for the current JsonWebToken
     *
     * @return
     */
    @Produces
    @Dependent
    Principal currentPrincipalOrNull() {
        return MPJWTProducer.getJWTPrincpal();
    }

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
