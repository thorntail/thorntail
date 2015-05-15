package org.wildfly.swarm.msc;

import org.wildfly.swarm.container.AbstractFractionDefaulter;
import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class MSCFractionDefaulter extends SimpleFractionDefaulter<MSCFraction> {

    public MSCFractionDefaulter() {
        super(MSCFraction.class);
    }

}
