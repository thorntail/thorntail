package org.wildfly.swarm.tools;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public interface ArtifactResolvingHelper {
    ArtifactSpec resolve(ArtifactSpec spec) throws Exception;

    Set<ArtifactSpec> resolveAll(Set<ArtifactSpec> specs) throws Exception;
}
