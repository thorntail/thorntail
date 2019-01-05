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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.repository.LocalArtifactRequest;
import org.eclipse.aether.repository.LocalArtifactResult;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wildfly.swarm.tools.ArtifactSpec;

public class MavenArtifactResolvingHelperTest {

    private RepositorySystemSession sessionMock;
    private LocalRepositoryManager localRepositoryManager;
    private ArtifactResolver resolver;
    private RepositorySystem system;

    public MavenArtifactResolvingHelperTest() {
        sessionMock = Mockito.mock(RepositorySystemSession.class);
        MirrorSelector mirrorSelectorMock = Mockito.mock(MirrorSelector.class);
        Mockito.when(sessionMock.getMirrorSelector()).thenReturn(mirrorSelectorMock);
        ProxySelector proxySelectorMock = Mockito.mock(ProxySelector.class);
        Mockito.when(sessionMock.getProxySelector()).thenReturn(proxySelectorMock);
        localRepositoryManager = Mockito.mock(LocalRepositoryManager.class);
        Mockito.when(sessionMock.getLocalRepositoryManager()).thenReturn(localRepositoryManager);
        resolver = Mockito.mock(ArtifactResolver.class);
        system = Mockito.mock(RepositorySystem.class);

    }

    @Test
    // SWARM-1173: swarm-plugin trying to download SNAPSHOTs from Maven Central
    public void propagateRepositoryPolicies() {
        MavenArtifactResolvingHelper artifactResolverHelper = new MavenArtifactResolvingHelper(null, null, sessionMock, null);
        ArtifactRepositoryPolicy testSnapshotPolicy = new ArtifactRepositoryPolicy(false, "hourly", "warn");
        ArtifactRepositoryPolicy testReleasesPolicy = new ArtifactRepositoryPolicy(true, "never", "warn");
        ArtifactRepository testingRepo = new MavenArtifactRepository("testing-repo", "http://testing-repo.org", new DefaultRepositoryLayout(), testSnapshotPolicy, testReleasesPolicy);
        artifactResolverHelper.remoteRepository(testingRepo);

        List<RemoteRepository> remoteRepos = artifactResolverHelper.getRemoteRepositories();

        Assert.assertTrue("Remote repositories " + remoteRepos + " do not contain expected testing repo " + testingRepo,
                          remoteRepos.stream().anyMatch(
                                  remoteRepo -> {
                                      RepositoryPolicy snapshotsPolicy = remoteRepo.getPolicy(true);
                                      RepositoryPolicy releasesPolicy = remoteRepo.getPolicy(false);
                                      return remoteRepo.getId().equals(testingRepo.getId()) &&
                                              !snapshotsPolicy.isEnabled() &&
                                              snapshotsPolicy.getUpdatePolicy().equals(testSnapshotPolicy.getUpdatePolicy()) &&
                                              snapshotsPolicy.getChecksumPolicy().equals(testSnapshotPolicy.getChecksumPolicy()) &&
                                              releasesPolicy.isEnabled() &&
                                              releasesPolicy.getUpdatePolicy().equals(testReleasesPolicy.getUpdatePolicy()) &&
                                              releasesPolicy.getChecksumPolicy().equals(testReleasesPolicy.getChecksumPolicy());
                                  })
        );
    }

    /**
     * Artifact extensions (suffixes) should be converted from artifact types according to this table:
     *
     * https://maven.apache.org/ref/3.6.0/maven-core/artifact-handlers.html
     */
    @Test
    public void testArtifactExtensions() throws Exception {
        // prepare mocks - always find an artifact in local repo
        Mockito.when(localRepositoryManager.find(Mockito.any(), Mockito.any(LocalArtifactRequest.class)))
                .thenReturn(new LocalArtifactResult(new LocalArtifactRequest())
                        .setAvailable(true).setFile(new File("test.jar")));


        MavenArtifactResolvingHelper artifactResolverHelper =
                new MavenArtifactResolvingHelper(resolver, system, sessionMock, null);
        // try to resolve artifacts with various packagings
        List<ArtifactSpec> artifacts = Arrays.asList(createSpec("ejb"), createSpec("pom"), createSpec("javadoc"));
        Set<ArtifactSpec> result = artifactResolverHelper.resolveAll(artifacts, false, false);


        Assert.assertEquals(3, result.size());
        ArgumentCaptor<LocalArtifactRequest> captor = ArgumentCaptor.forClass(LocalArtifactRequest.class);
        Mockito.verify(localRepositoryManager, Mockito.times(3)).find(Mockito.any(), captor.capture());
        // verify artifact extensions
        Assert.assertEquals("jar", captor.getAllValues().get(0).getArtifact().getExtension()); // packaging ejb
        Assert.assertEquals("pom", captor.getAllValues().get(1).getArtifact().getExtension()); // packaging pom
        Assert.assertEquals("jar", captor.getAllValues().get(2).getArtifact().getExtension()); // packaging javadoc
    }

    /**
     * @see #testArtifactExtensions()
     */
    @Test
    public void testManagedDependenciesExtensions() throws Exception {
        // prepare mocks
        // always find an artifact in local repo
        Mockito.when(localRepositoryManager.find(Mockito.any(), Mockito.any(LocalArtifactRequest.class)))
                .thenReturn(new LocalArtifactResult(new LocalArtifactRequest())
                        .setAvailable(true).setFile(new File("test.jar")));
        // return non-null when system.collectDependencies() is called
        CollectResult collectResult = new CollectResult(new CollectRequest());
        collectResult.setRoot(new DefaultDependencyNode((Artifact) null));
        Mockito.when(system.collectDependencies(Mockito.any(), Mockito.any(CollectRequest.class)))
                .thenReturn(collectResult);


        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.addDependency(createDependency("ejb-client"));
        dependencyManagement.addDependency(createDependency("javadoc"));
        dependencyManagement.addDependency(createDependency("pom"));

        MavenArtifactResolvingHelper artifactResolverHelper =
                new MavenArtifactResolvingHelper(resolver, system, sessionMock, dependencyManagement);
        // try to resolve artifacts with various packagings
        List<ArtifactSpec> artifacts = Arrays.asList(createSpec("ejb"), createSpec("pom"), createSpec("javadoc"));
        artifactResolverHelper.resolveAll(artifacts, true, false);


        ArgumentCaptor<CollectRequest> captor = ArgumentCaptor.forClass(CollectRequest.class);
        Mockito.verify(system).collectDependencies(Mockito.any(), captor.capture());
        // verify managed dependencies extensions
        Assert.assertEquals("jar", captor.getValue().getManagedDependencies().get(0).getArtifact().getExtension()); // type ejb-client
        Assert.assertEquals("jar", captor.getValue().getManagedDependencies().get(1).getArtifact().getExtension()); // type javadoc
        Assert.assertEquals("pom", captor.getValue().getManagedDependencies().get(2).getArtifact().getExtension()); // type pom
        // verify artifact extensions
        Assert.assertEquals("jar", captor.getValue().getDependencies().get(0).getArtifact().getExtension()); // packaging ejb
        Assert.assertEquals("pom", captor.getValue().getDependencies().get(1).getArtifact().getExtension()); // packaging pom
        Assert.assertEquals("jar", captor.getValue().getDependencies().get(2).getArtifact().getExtension()); // packaging javadoc
    }

    private ArtifactSpec createSpec(String packaging) {
        return new ArtifactSpec("compile", "io.thorntail", "test", "1.0", packaging, "", new File("test.jar"));
    }

    private Dependency createDependency(String type) {
        Dependency dependency = new Dependency();
        dependency.setGroupId("io.thorntail");
        dependency.setArtifactId("test");
        dependency.setVersion("1.0");
        dependency.setType(type);
        return dependency;
    }

}
