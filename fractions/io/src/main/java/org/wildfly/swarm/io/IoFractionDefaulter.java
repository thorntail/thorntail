package org.wildfly.swarm.io;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class IoFractionDefaulter extends SimpleFractionDefaulter<IoFraction> {
    public IoFractionDefaulter() {
        super(IoFraction.class);
    }

}
