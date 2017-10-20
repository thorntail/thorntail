package org.wildfly.swarm.mpjwtauth.deployment.auth.cdi;

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
