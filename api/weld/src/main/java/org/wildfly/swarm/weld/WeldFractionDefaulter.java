package org.wildfly.swarm.weld;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class WeldFractionDefaulter extends SimpleFractionDefaulter<WeldFraction> {

    public WeldFractionDefaulter() {
        super(WeldFraction.class);
    }

}
