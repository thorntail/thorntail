package org.wildfly.swarm.container.internal;

import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public interface FractionRegistry {
    void register(Fraction fraction);
}
