package org.wildfly.swarm.undertow;

import org.wildfly.swarm.container.SimpleFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class UndertowFractionDefaulter extends SimpleFractionDefaulter<UndertowFraction> {
    public UndertowFractionDefaulter() {
        super(UndertowFraction.class);
    }

}
