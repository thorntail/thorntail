/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian.resolver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.maven.settings.Settings;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenChecksumPolicy;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepository;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.MavenResolutionStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.NonTransitiveStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.TransitiveStrategy;
import org.jboss.shrinkwrap.resolver.impl.maven.ConfigurableMavenWorkingSessionImpl;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.wildfly.swarm.spi.api.internal.SwarmInternalProperties;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ShrinkwrapArtifactResolvingHelper implements ArtifactResolvingHelper {

    private static AtomicReference<ShrinkwrapArtifactResolvingHelper> INSTANCE = new AtomicReference<>();

    public static ShrinkwrapArtifactResolvingHelper defaultInstance() {
        return INSTANCE.updateAndGet(e -> {
            if (e != null) {
                return e;
            }

            MavenRemoteRepository jbossPublic =
                    MavenRemoteRepositories.createRemoteRepository("jboss-public-repository-group",
                                                                   "https://repository.jboss.org/nexus/content/groups/public/",
                                                                   "default");
            jbossPublic.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
            jbossPublic.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);


            MavenRemoteRepository gradleTools =
                    MavenRemoteRepositories.createRemoteRepository("gradle",
                                                                   "http://repo.gradle.org/gradle/libs-releases-local",
                                                                   "default");
            gradleTools.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
            gradleTools.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);

            Boolean offline = Boolean.valueOf(System.getProperty("swarm.resolver.offline", "false"));
            final ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                    .withMavenCentralRepo(true)
                    .withRemoteRepo(jbossPublic)
                    .withRemoteRepo(gradleTools)
                    .workOffline(offline);

            final String additionalRepos = System.getProperty(SwarmInternalProperties.BUILD_REPOS);
            if (additionalRepos != null) {
                Arrays.asList(additionalRepos.split(","))
                        .forEach(r -> {
                            MavenRemoteRepository repo =
                                    MavenRemoteRepositories.createRemoteRepository(r, r, "default");
                            repo.setChecksumPolicy(MavenChecksumPolicy.CHECKSUM_POLICY_IGNORE);
                            repo.setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER);
                            resolver.withRemoteRepo(repo);
                        });
            }


            ShrinkwrapArtifactResolvingHelper helper = new ShrinkwrapArtifactResolvingHelper(resolver);
            helper.session().setCache(new SimpleRepositoryCache());
            helper.session().setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_DAILY);
            helper.session().setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);

            return helper;
        });
    }

    public ConfigurableMavenResolverSystem getResolver() {
        return resolver;
    }

    public ShrinkwrapArtifactResolvingHelper(ConfigurableMavenResolverSystem resolver) {
        this.resolver = resolver;
        transferListener(new FailureReportingTransferListener());
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file == null) {
            final File localFile = new File(settings().getLocalRepository(), spec.jarRepoPath());
            if (localFile.exists()) {
                spec.file = localFile;
            } else {
                resetListeners();
                try {
                    final File file = this.resolver.resolve(spec.mavenGav()).withoutTransitivity().asSingleFile();
                    if (file != null) {
                        spec.file = file;
                    }
                } finally {
                    resolutionComplete();
                }
            }
        }

        return spec.file != null ? spec : null;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(final Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) {
        if (specs.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<ArtifactSpec> resolvedSpecs = new HashSet<>();
        // If we don't need transitive dependencies, then perform a dependency resolution only for the artifacts that do not have
        // a file reference.
        if (!transitive) {
            specs.stream().filter(s -> s.file != null).forEach(resolvedSpecs::add);
            specs.removeAll(resolvedSpecs);
            if (specs.isEmpty()) {
                return resolvedSpecs;
            }
        }

        MavenResolutionStrategy transitivityStrategy = (transitive ? TransitiveStrategy.INSTANCE : NonTransitiveStrategy.INSTANCE);

        resetListeners();
        final MavenResolvedArtifact[] artifacts =
                withResolver(r -> {
                    specs.forEach(spec -> r.addDependency(createMavenDependency(spec)));
                    return r.resolve()
                            .using(transitivityStrategy)
                            .as(MavenResolvedArtifact.class);
                });

        resolvedSpecs.addAll(
                Arrays.stream(artifacts).map(artifact -> {
                    final MavenCoordinate coord = artifact.getCoordinate();
                    return new ArtifactSpec(artifact.getScope().toString(),
                                            coord.getGroupId(),
                                            coord.getArtifactId(),
                                            coord.getVersion(),
                                            coord.getPackaging().getId(),
                                            coord.getClassifier(),
                                            artifact.asFile());
                }).collect(Collectors.toSet())
        );
        return resolvedSpecs;
    }

    public MavenDependency createMavenDependency(final ArtifactSpec spec) {
        final MavenCoordinate newCoordinate = MavenCoordinates.createCoordinate(
                spec.groupId(),
                spec.artifactId(),
                spec.version(),
                PackagingType.of(spec.type()),
                spec.classifier());
        return MavenDependencies.createDependency(newCoordinate, ScopeType.fromScopeType(spec.scope), false);
    }

    public ShrinkwrapArtifactResolvingHelper repositoryListener(final RepositoryListener l) {
        this.repositoryListener = l;

        return this;
    }

    public ShrinkwrapArtifactResolvingHelper transferListener(final CompletableTransferListener l) {
        this.transferListener = l;

        return this;
    }

    public MavenResolvedArtifact[] withResolver(ResolverAction action) {
        resetListeners();
        try {
            return action.resolve(this.resolver);
        } finally {
            resolutionComplete();
        }
    }

    private void resetListeners() {
        final DefaultRepositorySystemSession session = session();
        session.setRepositoryListener(this.repositoryListener);
        session.setTransferListener(this.transferListener);
    }

    private void resolutionComplete() {
        if (this.transferListener != null) {
            this.transferListener.complete();
        }
    }

    private DefaultRepositorySystemSession session() {
        return (DefaultRepositorySystemSession) invokeWorkingSessionMethod("getSession");
    }

    private Settings settings() {
        return (Settings) invokeWorkingSessionMethod("getSettings");
    }

    private Object invokeWorkingSessionMethod(final String methodName) {
        try {
            final Method method = ConfigurableMavenWorkingSessionImpl.class.getDeclaredMethod(methodName);
            method.setAccessible(true);

            return method.invoke(((MavenWorkingSessionContainer) this.resolver).getMavenWorkingSession());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to invoke " + methodName, e);
        }
    }

    private final ConfigurableMavenResolverSystem resolver;

    private CompletableTransferListener transferListener;

    private RepositoryListener repositoryListener;

    public interface ResolverAction {
        MavenResolvedArtifact[] resolve(ConfigurableMavenResolverSystem resolver);
    }
}
