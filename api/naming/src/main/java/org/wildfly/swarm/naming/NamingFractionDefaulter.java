package org.wildfly.swarm.naming;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class NamingFractionDefaulter extends SimpleFractionDefaulter<NamingFraction> {
    public NamingFractionDefaulter() {
        super(NamingFraction.class);
    }

}
