package org.wildfly.swarm.arquillian.adapter;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class ShrinkwrapArtifactResolvingHelper implements ArtifactResolvingHelper {

    private final ConfigurableMavenResolverSystem resolver;

    public ShrinkwrapArtifactResolvingHelper(ConfigurableMavenResolverSystem resolver) {
        this.resolver = resolver;
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) throws Exception {
        if ( spec.file != null ) {
            return spec;
        }
        File file = this.resolver.resolve(spec.coordinates()).withoutTransitivity().asSingleFile();
        if ( file == null ) {
            return null;
        }
        spec.file = file;
        return spec;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Set<ArtifactSpec> specs) throws Exception {
        final MavenResolvedArtifact[] artifacts = this.resolver
                .resolve(specs.stream().map(s -> s.coordinates()).collect(Collectors.toList()))
                .withTransitivity()
                .as(MavenResolvedArtifact.class);

        return Arrays.stream(artifacts).map(artifact -> {
            final MavenCoordinate coord = artifact.getCoordinate();
            return new ArtifactSpec("compile",
                                    coord.getGroupId(),
                                    coord.getArtifactId(),
                                    coord.getVersion(),
                                    coord.getPackaging().getId(),
                                    coord.getClassifier(),
                                    artifact.asFile());
        }).collect(Collectors.toSet());
    }
}
