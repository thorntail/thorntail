package org.wildfly.swarm.mpjwtauth.deployment.auth.cdi;

import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

public class ClaimLiteral extends AnnotationLiteral<Claim> implements Claim {
    public String value() {
        return "";
    }

    public Claims standard() {
        return Claims.UNKNOWN;
    }
}
