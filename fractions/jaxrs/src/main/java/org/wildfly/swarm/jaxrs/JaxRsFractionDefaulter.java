package org.wildfly.swarm.jaxrs;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class JaxRsFractionDefaulter extends SimpleFractionDefaulter<JaxRsFraction> {

    public JaxRsFractionDefaulter() {
        super(JaxRsFraction.class);
    }

}
