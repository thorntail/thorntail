package org.wildfly.swarm.plugin;

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

/**
 * @author Bob McWhirter
 */
public class MavenArtifactResolvingHelper implements ArtifactResolvingHelper {

    private ArtifactResolver resolver;
    protected RepositorySystemSession session;
    protected List<RemoteRepository> remoteRepositories = new ArrayList<>();


    public MavenArtifactResolvingHelper(ArtifactResolver resolver, RepositorySystemSession session, List<ArtifactRepository> remoteRepositories) {
        this.resolver = resolver;
        this.session = session;
        for (ArtifactRepository each : remoteRepositories) {
            RemoteRepository.Builder builder = new RemoteRepository.Builder(each.getId(), "default", each.getUrl());
            this.remoteRepositories.add(builder.build());
        }

        this.remoteRepositories.add(new RemoteRepository.Builder("jboss-public-repository-group", "default", "http://repository.jboss.org/nexus/content/groups/public/").build());
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
            return null;
        }

        return null;

    }


}
