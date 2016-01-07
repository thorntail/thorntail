package org.wildfly.swarm.bootstrap.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenResolver;

/**
 * @author Bob McWhirter
 */
public class MultiMavenResolver implements MavenResolver {


    private List<MavenResolver> resolvers = new ArrayList<>();

    public MultiMavenResolver() {

    }

    public void addResolver(MavenResolver resolver) {
        this.resolvers.add( resolver );
    }

    @Override
    public File resolveArtifact(ArtifactCoordinates coordinates, String packaging) throws IOException {

        for (MavenResolver resolver : this.resolvers) {
            File result = resolver.resolveArtifact(coordinates, packaging);
            if ( result != null ) {
                return result;
            }
        }

        return null;
    }
}
