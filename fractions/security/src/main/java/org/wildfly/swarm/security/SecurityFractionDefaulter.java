package org.wildfly.swarm.security;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class SecurityFractionDefaulter extends SimpleFractionDefaulter<SecurityFraction> {
    public SecurityFractionDefaulter() {
        super(SecurityFraction.class);
    }

}
