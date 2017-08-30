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

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MavenArtifactResolvingHelperTest {

    @Test
    // SWARM-1173: swarm-plugin trying to download SNAPSHOTs from Maven Central
    public void propagateRepositoryPolicies() {
        RepositorySystemSession sessionMock = Mockito.mock(RepositorySystemSession.class);
        MirrorSelector mirrorSelectorMock = Mockito.mock(MirrorSelector.class);
        Mockito.when(sessionMock.getMirrorSelector()).thenReturn(mirrorSelectorMock);
        ProxySelector proxySelectorMock = Mockito.mock(ProxySelector.class);
        Mockito.when(sessionMock.getProxySelector()).thenReturn(proxySelectorMock);

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

}
