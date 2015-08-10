package org.wildfly.swarm.keycloak;

import org.jboss.shrinkwrap.api.Assignable;

/**
 * @author Bob McWhirter
 */
public interface Secured extends Assignable {

    SecurityConstraint protect();
    SecurityConstraint protect(String urlPattern);


}
