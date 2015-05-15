package org.wildfly.swarm.weld;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Ken Finnigan
 */
public class WeldJaxRsFractionDefaulter extends SimpleFractionDefaulter<WeldJaxRsFraction> {

    public WeldJaxRsFractionDefaulter() {
        super(WeldJaxRsFraction.class);
    }

}
