package org.wildfly.swarm.tools;

/**
 * @author Bob McWhirter
 */
public interface ArtifactResolvingHelper {
    ArtifactSpec resolve(ArtifactSpec spec) throws Exception;
}
