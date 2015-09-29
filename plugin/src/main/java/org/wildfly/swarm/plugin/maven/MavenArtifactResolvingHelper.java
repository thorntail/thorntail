package org.wildfly.swarm.plugin.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactResolvingHelper implements ArtifactResolvingHelper {

    private ArtifactResolver resolver;
    protected RepositorySystemSession session;
    protected List<RemoteRepository> remoteRepositories = new ArrayList<>();


    public MavenArtifactResolvingHelper(ArtifactResolver resolver, RepositorySystemSession session) {
        this.resolver = resolver;
        this.session = session;
        this.remoteRepositories.add(new RemoteRepository.Builder("jboss-public-repository-group", "default", "http://repository.jboss.org/nexus/content/groups/public/").build());
    }

    public void remoteRepository(ArtifactRepository repo) {
        RemoteRepository.Builder builder = new RemoteRepository.Builder(repo.getId(), "default", repo.getUrl());
        this.remoteRepositories.add(builder.build());
    }

    public void remoteRepository(RemoteRepository repo) {
        this.remoteRepositories.add( repo );
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file != null) {
            return spec;
        }

        ArtifactRequest request = new ArtifactRequest();

        DefaultArtifact artifact = new DefaultArtifact(spec.groupId, spec.artifactId, spec.classifier, spec.packaging, spec.version);

        request.setArtifact(artifact);
        request.setRepositories(this.remoteRepositories);

        try {
            ArtifactResult result = resolver.resolveArtifact(this.session, request);

            if (result.isResolved()) {
                spec.file = result.getArtifact().getFile();
                return spec;
            }
        } catch (ArtifactResolutionException e) {
            System.err.println( "ERR " + e );
            e.printStackTrace();
            return null;
        }

        return null;

    }

    @Override
    public Set<ArtifactSpec> resolveAll(Set<ArtifactSpec> specs) throws Exception {
        // TODO: determine if we need to implement this. Current usage of BuildTool doesn't need it for mvn
        throw new UnsupportedOperationException("Not implemented");
    }

}
