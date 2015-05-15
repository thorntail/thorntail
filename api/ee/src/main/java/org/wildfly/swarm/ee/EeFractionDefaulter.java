package org.wildfly.swarm.ee;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class EeFractionDefaulter extends SimpleFractionDefaulter<EeFraction> {

    public EeFractionDefaulter() {
        super(EeFraction.class);
    }

}
