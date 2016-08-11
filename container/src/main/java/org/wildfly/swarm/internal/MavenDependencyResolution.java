package org.wildfly.swarm.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Vetoed;

import org.wildfly.swarm.bootstrap.modules.MavenResolvers;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

/**
 * @author Heiko Braun
 * @since 18/07/16
 */
@Vetoed
public class MavenDependencyResolution implements DependencyResolution {

    public MavenDependencyResolution(WildFlySwarmDependenciesConf deps) {
        this.deps = deps;
    }

    @Override
    public Set<String> resolve(List<String> exclusions) throws IOException {
        final Set<String> archivePaths = new HashSet<>();
        for (MavenArtifactDescriptor each : this.deps.getPrimaryDependencies()) {
            if (exclusions.contains(each.groupId())) {
                continue;
            }
            final File artifact = MavenResolvers.get().resolveJarArtifact(each.mscCoordinates());
            archivePaths.add(artifact.getAbsolutePath());
        }
        return archivePaths;
    }

    final private WildFlySwarmDependenciesConf deps;
}
