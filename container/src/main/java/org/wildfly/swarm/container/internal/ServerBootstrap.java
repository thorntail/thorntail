package org.wildfly.swarm.container.internal;

import java.util.Collection;

import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Bob McWhirter
 */
public interface ServerBootstrap {
    Server bootstrap(Collection<Fraction> explicitlyInstalledFractions) throws Exception;
}
