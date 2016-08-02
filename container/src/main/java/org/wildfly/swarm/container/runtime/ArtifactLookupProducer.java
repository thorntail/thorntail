package org.wildfly.swarm.container.runtime;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.ArtifactLookup;

/**
 * @author Bob McWhirter
 */
@Singleton
public class ArtifactLookupProducer {

    @Produces @Singleton
    public ArtifactLookup lookup() {
        return ArtifactLookup.get();
    }
}
