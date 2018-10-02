
/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.runner.maven;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.wildfly.swarm.maven.utils.RepositorySystemSessionWrapper;
import org.wildfly.swarm.runner.cache.ArtifactResolutionCache;
import org.wildfly.swarm.runner.cache.DependencyResolutionCache;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wildfly.swarm.runner.maven.MavenInitializer.buildRemoteRepository;
import static org.wildfly.swarm.runner.maven.MavenInitializer.newRepositorySystem;
import static org.wildfly.swarm.runner.maven.MavenInitializer.newSession;
import static org.wildfly.swarm.runner.utils.StringUtils.randomAlphabetic;

public class CachingArtifactResolvingHelper implements ArtifactResolvingHelper {

    public CachingArtifactResolvingHelper() {
        repoSystem = newRepositorySystem();

        session = newSession(repoSystem);

        this.remoteRepositories.add(buildRemoteRepository(
                session,
                "jboss-public-repository-group",
                "https://repository.jboss.org/nexus/content/groups/public/",
                null,
                null));
        this.remoteRepositories.add(buildRemoteRepository(
                session,
                "maven-central",
                "https://repo.maven.apache.org/maven2/",
                null,
                null));

        addUserRepositories();
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file == null) {
            File maybeFile = artifactCache.getCachedFile(spec);
            if (!artifactCache.isKnownFailure(spec) && maybeFile == null) {
                System.out.println("no cached file for " + spec.mscGav());
                maybeFile = resolveArtifactFile(spec);
                artifactCache.storeArtifactFile(spec, maybeFile);
            }

            if (maybeFile == null) {
                artifactCache.storeResolutionFailure(spec);
                return null;
            }
            spec.file = maybeFile;
        }

        return spec;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) throws Exception {
        if (specs.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<ArtifactSpec> toResolve = specs;
        if (transitive) {
            toResolve = resolveDependencies(specs, defaultExcludes);
        }

        return resolveInParallel(toResolve);
    }

    private Set<ArtifactSpec> resolveInParallel(Collection<ArtifactSpec> toResolve) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
        List<Callable<ArtifactSpec>> callable = toResolve.stream()
                .map(spec -> (Callable<ArtifactSpec>) (() -> this.resolve(spec)))
                .collect(Collectors.toList());

        List<Future<ArtifactSpec>> futures = threadPool.invokeAll(
                callable
        );
        Set<ArtifactSpec> result = futures.stream()
                .map(this::safeGet)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        threadPool.shutdown();
        return result;
    }

    private <T> T safeGet(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addUserRepositories() {
        String repositoriesProperty = System.getProperty("thorntail.runner.repositories");
        if (repositoriesProperty != null) {
            Stream.of(repositoriesProperty.split(","))
                    .forEach(this::addUserRepository);

        }
    }

    private void addUserRepository(String repositoryAsString) {
        String[] split = repositoryAsString.split("#");
        String url = split[0];
        String username = null;
        String password = null;

        if (split.length > 2) {
            username = split[1];
            password = split[2];
        }
        this.remoteRepositories.add(buildRemoteRepository(session, randomAlphabetic(8), url, username, password));
    }


    private Collection<ArtifactSpec> resolveDependencies(final Collection<ArtifactSpec> specs, boolean defaultExcludes) throws DependencyCollectionException {
        List<ArtifactSpec> dependencyNodes = dependencyCache.getCachedDependencies(specs, defaultExcludes);
        if (dependencyNodes == null) {
            List<Dependency> dependencies =
                    specs.stream()
                            .map(this::artifact)
                            .map(a -> new Dependency(a, "compile"))
                            .collect(Collectors.toList());

            CollectRequest collectRequest = new CollectRequest(dependencies, null, remoteRepositories);

            RepositorySystemSession session = new RepositorySystemSessionWrapper(this.session, defaultExcludes);
            CollectResult result = this.repoSystem.collectDependencies(session, collectRequest);
            PreorderNodeListGenerator gen = new PreorderNodeListGenerator();
            result.getRoot().accept(gen);
            dependencyNodes = gen.getNodes()
                    .stream()
                    .map(
                            node -> {
                                Artifact artifact = node.getArtifact();
                                return new ArtifactSpec(node.getDependency().getScope(),
                                        artifact.getGroupId(),
                                        artifact.getArtifactId(),
                                        artifact.getVersion(),
                                        artifact.getExtension(),
                                        artifact.getClassifier(),
                                        artifact.getFile());
                            }
                    ).collect(Collectors.toList());


            dependencyCache.storeCachedDependencies(specs, dependencyNodes, defaultExcludes);
        }

        Collection<ArtifactSpec> result = resolveDependencies(dependencyNodes);
        return result;
    }

    private Collection<ArtifactSpec> resolveDependencies(List<ArtifactSpec> dependencyNodes) {
        long start = System.currentTimeMillis();
        // if dependencies were previously resolved, we don't need to resolve using remote repositories
        dependencyNodes = new ArrayList<>(dependencyNodes);

        return dependencyNodes.parallelStream()
                .filter(node -> !"system".equals(node.scope))
                .map(node -> new ArtifactSpec(node.scope,
                        node.groupId(),
                        node.artifactId(),
                        node.version(),
                        "bundle".equals(node.type()) ? "jar" : node.type(),
                        node.classifier(),
                        null))
                .map(this::resolve)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private DefaultArtifact artifact(ArtifactSpec spec) {
        String type = spec.type();
        type = "bundle".equals(type) ? "jar" : type;
        return new DefaultArtifact(spec.groupId(), spec.artifactId(), spec.classifier(),
                type, spec.version());
    }

    private File resolveArtifactFile(ArtifactSpec spec) {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact(spec));

        remoteRepositories.forEach(request::addRepository);

        try {
            ArtifactResult artifactResult = repoSystem.resolveArtifact(session, request);

            return artifactResult.isResolved()
                    ? artifactResult.getArtifact().getFile()
                    : null;
        } catch (ArtifactResolutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final DependencyResolutionCache dependencyCache = DependencyResolutionCache.INSTANCE;
    private final ArtifactResolutionCache artifactCache = ArtifactResolutionCache.INSTANCE;
    private final List<RemoteRepository> remoteRepositories = new ArrayList<>();
    private final RepositorySystem repoSystem;
    private final RepositorySystemSession session;

}
