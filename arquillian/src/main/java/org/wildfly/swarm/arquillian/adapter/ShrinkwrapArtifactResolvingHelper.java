package org.wildfly.swarm.arquillian.adapter;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryListener;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionImpl;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class ShrinkwrapArtifactResolvingHelper implements ArtifactResolvingHelper {

    public ShrinkwrapArtifactResolvingHelper(ConfigurableMavenResolverSystem resolver) {
        this.resolver = resolver;
        transferListener(new FailureReportingTransferListener());
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        resetListeners();
        try {
            if (spec.file != null) {
                return spec;
            }
            File file = this.resolver.resolve(spec.coordinates()).withoutTransitivity().asSingleFile();
            if (file == null) {
                return null;
            }
            spec.file = file;
        } finally {
            completeTransferListener();
        }

        return spec;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Set<ArtifactSpec> specs) {
        resetListeners();
        final MavenResolvedArtifact[] artifacts;
        try {
            artifacts = this.resolver
                    .resolve(specs.stream().map(ArtifactSpec::coordinates).collect(Collectors.toList()))
                    .withTransitivity()
                    .as(MavenResolvedArtifact.class);
        } finally {
            completeTransferListener();
        }

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

    public ShrinkwrapArtifactResolvingHelper repositoryListener(final RepositoryListener l) {
        this.repositoryListener = l;

        return this;
    }

    public ShrinkwrapArtifactResolvingHelper transferListener(final CompleteableTransferListener l) {
        this.transferListener = l;

        return this;
    }

    private void resetListeners() {
        final DefaultRepositorySystemSession session = session();
        session.setRepositoryListener(this.repositoryListener);
        session.setTransferListener(this.transferListener);
    }

    private void completeTransferListener() {
        if (this.transferListener != null) {
            this.transferListener.complete();
        }
    }

    private DefaultRepositorySystemSession session() {
        final MavenWorkingSession session = ((MavenWorkingSessionContainer) this.resolver).getMavenWorkingSession();
        try {
            final Field innerSession = MavenWorkingSessionImpl.class.getDeclaredField("session");
            innerSession.setAccessible(true);

            return (DefaultRepositorySystemSession)innerSession.get(session);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to access maven session", e);
        }
    }

    private final ConfigurableMavenResolverSystem resolver;
    private CompleteableTransferListener transferListener;
    private RepositoryListener repositoryListener;
}
