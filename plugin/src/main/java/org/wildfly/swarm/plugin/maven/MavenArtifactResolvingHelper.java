/**
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
package org.wildfly.swarm.plugin.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.wildfly.swarm.tools.ArtifactResolvingHelper;
import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactResolvingHelper implements ArtifactResolvingHelper {

    public MavenArtifactResolvingHelper(ArtifactResolver resolver,
                                        RepositorySystem system,
                                        RepositorySystemSession session,
                                        Proxy proxy) {
        this.resolver = resolver;
        this.system = system;
        this.session = session;
        this.proxy = proxy;
        this.remoteRepositories.add(buildRemoteRepository("jboss-public-repository-group",
                                                          "http://repository.jboss.org/nexus/content/groups/public/",
                                                          null,
                                                          this.proxy));
    }

    public void remoteRepository(ArtifactRepository repo) {
        remoteRepository(buildRemoteRepository(repo.getId(), repo.getUrl(), repo.getAuthentication(), this.proxy));
    }

    public void remoteRepository(RemoteRepository repo) {
        this.remoteRepositories.add(repo);
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) {
        if (spec.file == null) {
            final DefaultArtifact artifact = new DefaultArtifact(spec.groupId(), spec.artifactId(), spec.classifier(),
                    spec.type(), spec.version());

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
                    System.err.println("ERR " + e);
                    e.printStackTrace();
                }
            }
        }

        return spec.file != null ? spec : null;

    }

    @Override
    public Set<ArtifactSpec> resolveAll(Set<ArtifactSpec> specs) throws Exception {
        if (specs.isEmpty()) {
            return specs;
        }

        final CollectRequest request = new CollectRequest();
        request.setRepositories(this.remoteRepositories);

        specs.forEach(spec -> request
                .addDependency(new Dependency(new DefaultArtifact(spec.groupId(),
                        spec.artifactId(),
                        spec.classifier(),
                        spec.type(),
                        spec.version()),
                        "compile")));

        RepositorySystemSession tempSession =
                new RepositorySystemSessionWrapper(this.session,
                                                   new ConflictResolver(new NewestVersionSelector(),
                                                                        new JavaScopeSelector(),
                                                                        new SimpleOptionalitySelector(),
                                                                        new JavaScopeDeriver()
                                                   )
                );

        CollectResult result = this.system.collectDependencies(tempSession, request);

        PreorderNodeListGenerator gen = new PreorderNodeListGenerator();
        result.getRoot().accept(gen);
        List<DependencyNode> nodes = gen.getNodes();

        resolveDependenciesInParallel(nodes);

        return nodes.stream()
                .filter(node -> !"system".equals(node.getDependency().getScope()))
                .map(node -> {
                    final Artifact artifact = node.getArtifact();

                    return new ArtifactSpec(node.getDependency().getScope(),
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            artifact.getVersion(),
                            artifact.getExtension(),
                            artifact.getClassifier(),
                            null);
                })
                .map(this::resolve)
                .filter(x -> x != null)
                .collect(Collectors.toSet());
    }

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

    protected static RemoteRepository buildRemoteRepository(final String id, final String url,
                                                            final Authentication auth, final Proxy proxy) {
        RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);
        if (auth != null &&
                auth.getUsername() != null &&
                auth.getPassword() != null) {
            builder.setAuthentication(new AuthenticationBuilder()
                                              .addUsername(auth.getUsername())
                                              .addPassword(auth.getPassword()).build());
        }

        if (proxy != null) {
            builder.setProxy(proxy);
        }

        return builder.build();
    }

    final protected RepositorySystemSession session;

    final protected List<RemoteRepository> remoteRepositories = new ArrayList<>();

    final private ArtifactResolver resolver;

    final private RepositorySystem system;

    final private Proxy proxy;
}
