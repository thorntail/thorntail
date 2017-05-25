package org.wildfly.swarm.security;

import javax.annotation.security.RolesAllowed;

/**
 *
 * @author Juan Gonzalez
 *
 */
public class SecurityEndpoint {

    @RolesAllowed("admin")
    public String foo() {
        return "FOO";
    }

}