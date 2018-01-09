package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenResolver;

public interface ArtifactResolver {

    ArtifactResolution resolveArtifact(ArtifactCoordinates coords, String packaging) throws IOException;

    default ArtifactResolution resolveJarArtifact(ArtifactCoordinates coords) throws IOException {
        return resolveArtifact(coords, "jar");

    }

    static ArtifactResolver wrapJBossModulesResolver(MavenResolver resolver) {
        return (coords, packaging) -> {
            File file = resolver.resolveArtifact(coords, packaging);
            if (file != null) {
                return new ArtifactResolution.FileArtifactResolution(coords, packaging, file);
            }
            return null;
        };
    }
}
