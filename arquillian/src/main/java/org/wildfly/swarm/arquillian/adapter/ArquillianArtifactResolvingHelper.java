package org.wildfly.swarm.arquillian.adapter;

import java.io.File;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 */
public class ArquillianArtifactResolvingHelper implements ArtifactResolvingHelper {

    private final ConfigurableMavenResolverSystem resolver;

    public ArquillianArtifactResolvingHelper(ConfigurableMavenResolverSystem resolver) {
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
}
