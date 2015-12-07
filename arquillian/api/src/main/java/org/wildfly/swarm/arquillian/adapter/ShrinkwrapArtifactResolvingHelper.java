/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            File file = this.resolver.resolve(spec.mavenGav()).withoutTransitivity().asSingleFile();
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
                    .resolve(specs.stream().map(ArtifactSpec::mavenGav).collect(Collectors.toList()))
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
