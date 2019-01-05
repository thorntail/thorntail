/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.maven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.model.DependencyManagement;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.wildfly.swarm.maven.utils.RepositorySystemSessionWrapper;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactResolvingHelper implements ArtifactResolvingHelper {

    public static final ArtifactRepositoryPolicy ENABLED_POLICY = new ArtifactRepositoryPolicy(true, null, null);

    public static final ArtifactRepositoryPolicy DISABLED_POLICY = new ArtifactRepositoryPolicy(false, null, null);

    public MavenArtifactResolvingHelper(ArtifactResolver resolver,
                                        RepositorySystem system,
                                        RepositorySystemSession session,
                                        DependencyManagement dependencyManagement) {
        this.resolver = resolver;
        this.system = system;
        this.session = session;
        this.dependencyManagement = dependencyManagement;
        this.remoteRepositories.add(buildRemoteRepository("jboss-public-repository-group",
                                                          "https://repository.jboss.org/nexus/content/groups/public/",
                                                          null,
                                                          ENABLED_POLICY,
                                                          DISABLED_POLICY));
    }

    public void remoteRepository(ArtifactRepository repo) {
        remoteRepository(buildRemoteRepository(repo.getId(), repo.getUrl(), repo.getAuthentication(), repo.getReleases(), repo.getSnapshots()));
    }

    public void remoteRepository(RemoteRepository repo) {
        this.remoteRepositories.add(repo);
    }

    public List<RemoteRepository> getRemoteRepositories() {
        return this.remoteRepositories;
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file == null) {
            final DefaultArtifact artifact = new DefaultArtifact(spec.groupId(), spec.artifactId(), spec.classifier(),
                    typeToExtension(spec.type()), spec.version(), new DefaultArtifactType(spec.type()));

            final LocalArtifactResult localResult = this.session.getLocalRepositoryManager()
                    .find(this.session, new LocalArtifactRequest(artifact, this.remoteRepositories, null));
            if (localResult.isAvailable()) {
                spec.file = localResult.getFile();
            } else {
                try {
                    final ArtifactResult result = resolver.resolveArtifact(this.session,
                                                                           new ArtifactRequest(artifact,
                                                                                               this.remoteRepositories,
                                                                                               null));
                    if (result.isResolved()) {
                        spec.file = result.getArtifact().getFile();
                    }
                } catch (ArtifactResolutionException e) {
                    e.printStackTrace();
                }
            }
        }

        return spec.file != null ? spec : null;
    }

    @Override
    public Set<ArtifactSpec> resolveAll(Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) throws Exception {
        if (specs.isEmpty()) {
            return Collections.emptySet();
        }
        List<DependencyNode> nodes;
        if (transitive) {

            final CollectRequest request = new CollectRequest();
            request.setRepositories(this.remoteRepositories);

            // SWARM-1031
            if (this.dependencyManagement != null) {
                List<Dependency> managedDependencies = this.dependencyManagement.getDependencies()
                        .stream()
                        .map(mavenDep -> {
                            DefaultArtifact artifact = new DefaultArtifact(
                                    mavenDep.getGroupId(),
                                    mavenDep.getArtifactId(),
                                    mavenDep.getClassifier(),
                                    typeToExtension(mavenDep.getType()),
                                    mavenDep.getVersion(),
                                    new DefaultArtifactType(mavenDep.getType())
                            );
                            return new Dependency(artifact, mavenDep.getScope());
                        })
                        .collect(Collectors.toList());

                request.setManagedDependencies(managedDependencies);
            }

            specs.forEach(spec -> request
                    .addDependency(new Dependency(new DefaultArtifact(spec.groupId(),
                            spec.artifactId(),
                            spec.classifier(),
                            typeToExtension(spec.type()),
                            spec.version(),
                            new DefaultArtifactType(spec.type())),
                            "compile")));

            RepositorySystemSession tempSession
                    = new RepositorySystemSessionWrapper(this.session, defaultExcludes
            );
            CollectResult result = this.system.collectDependencies(tempSession, request);
            PreorderNodeListGenerator gen = new PreorderNodeListGenerator();
            result.getRoot().accept(gen);
            nodes = gen.getNodes();
        } else {
            nodes = new ArrayList<>();
            for (ArtifactSpec spec : specs) {
                Dependency dependency = new Dependency(new DefaultArtifact(spec.groupId(),
                        spec.artifactId(),
                        spec.classifier(),
                        typeToExtension(spec.type()),
                        spec.version(),
                        new DefaultArtifactType(spec.type())),
                        "compile");
                DefaultDependencyNode node = new DefaultDependencyNode(dependency);
                nodes.add(node);
            }
        }

        List<DependencyNode> extraDependencies = ExtraArtifactsHandler.getExtraDependencies(nodes);

        nodes.addAll(extraDependencies);

        resolveDependenciesInParallel(nodes);

        return nodes.stream()
                .filter(node -> !"system".equals(node.getDependency().getScope()))
                .map(node -> {
                    final Artifact artifact = node.getArtifact();
                    return new ArtifactSpec(node.getDependency().getScope(),
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            artifact.getVersion(),
                            artifact.getProperty(ArtifactProperties.TYPE, null),
                            artifact.getClassifier(),
                            null);
                })
                .map(this::resolve)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String typeToExtension(String type) {
        switch (type) {
            case "pom":
            case "jar":
            case "war":
            case "ear":
            case "rar":
                return type;
            default:
                return "jar";
        }
    }

    /**
     * This is needed to speed up things.
     */
    private void resolveDependenciesInParallel(List<DependencyNode> nodes) {
        List<ArtifactRequest> artifactRequests = nodes.stream()
                .map(node -> new ArtifactRequest(node.getArtifact(), this.remoteRepositories, null))
                .collect(Collectors.toList());

        try {
            this.resolver.resolveArtifacts(this.session, artifactRequests);
        } catch (ArtifactResolutionException e) {
            // ignore, error will be printed by resolve(ArtifactSpec)
        }
    }

    private RemoteRepository buildRemoteRepository(final String id, final String url, final Authentication auth,
                                                   final ArtifactRepositoryPolicy releasesPolicy, final ArtifactRepositoryPolicy snapshotsPolicy) {
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);
        if (auth != null
                && auth.getUsername() != null
                && auth.getPassword() != null) {
            builder.setAuthentication(new AuthenticationBuilder()
                                              .addUsername(auth.getUsername())
                                              .addPassword(auth.getPassword()).build());
        }

        builder.setSnapshotPolicy(new RepositoryPolicy(snapshotsPolicy.isEnabled(), snapshotsPolicy.getUpdatePolicy(), snapshotsPolicy.getChecksumPolicy()));
        builder.setReleasePolicy(new RepositoryPolicy(releasesPolicy.isEnabled(), releasesPolicy.getUpdatePolicy(), releasesPolicy.getChecksumPolicy()));

        RemoteRepository repository = builder.build();

        final RemoteRepository mirror = session.getMirrorSelector().getMirror(repository);

        if (mirror != null) {
            final org.eclipse.aether.repository.Authentication mirrorAuth = session.getAuthenticationSelector()
                    .getAuthentication(mirror);
            RemoteRepository.Builder mirrorBuilder = new RemoteRepository.Builder(mirror)
                    .setId(repository.getId())
                    .setSnapshotPolicy(new RepositoryPolicy(snapshotsPolicy.isEnabled(), snapshotsPolicy.getUpdatePolicy(), snapshotsPolicy.getChecksumPolicy()))
                    .setReleasePolicy(new RepositoryPolicy(releasesPolicy.isEnabled(), releasesPolicy.getUpdatePolicy(), releasesPolicy.getChecksumPolicy()));
            if (mirrorAuth != null) {
                mirrorBuilder.setAuthentication(mirrorAuth);
            }
            repository = mirrorBuilder.build();
        }

        Proxy proxy = session.getProxySelector().getProxy(repository);

        if (proxy != null) {
            repository = new RemoteRepository.Builder(repository).setProxy(proxy).build();
        }

        return repository;
    }

    protected final RepositorySystemSession session;

    private final DependencyManagement dependencyManagement;

    protected final List<RemoteRepository> remoteRepositories = new ArrayList<>();

    private final ArtifactResolver resolver;

    private final RepositorySystem system;
}
