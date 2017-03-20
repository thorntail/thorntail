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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.version.TestHelper;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class NewestVersionSelectorTest {

    @Test
    public void betaVersionComparison() throws Exception {
        NewestVersionSelector selector = new NewestVersionSelector();
        List<ConflictResolver.ConflictItem> items = new ArrayList<>();

        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Beta3"), 0, 0));
        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Beta5-SNAPSHOT"), 0, 0));

        ConflictResolver.ConflictContext context = new ConflictResolver.ConflictContext(null, null, null, items);
        selector.selectVersion(context);

        assertThat(context.getWinner().getNode().getVersion().toString()).isEqualTo("1.0.0.Beta5-SNAPSHOT");
    }

    @Test
    public void snapshotVersionComparison() throws Exception {
        NewestVersionSelector selector = new NewestVersionSelector();
        List<ConflictResolver.ConflictItem> items = new ArrayList<>();

        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Beta3"), 0, 0));
        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Beta3-SNAPSHOT"), 0, 0));

        ConflictResolver.ConflictContext context = new ConflictResolver.ConflictContext(null, null, null, items);
        selector.selectVersion(context);

        assertThat(context.getWinner().getNode().getVersion().toString()).isEqualTo("1.0.0.Beta3");
    }

    @Test
    public void alphaToBetaVersionComparison() throws Exception {
        NewestVersionSelector selector = new NewestVersionSelector();
        List<ConflictResolver.ConflictItem> items = new ArrayList<>();

        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Alpha8"), 0, 0));
        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Beta3"), 0, 0));

        ConflictResolver.ConflictContext context = new ConflictResolver.ConflictContext(null, null, null, items);
        selector.selectVersion(context);

        assertThat(context.getWinner().getNode().getVersion().toString()).isEqualTo("1.0.0.Beta3");
    }

    @Test
    public void finalVersionComparison() throws Exception {
        NewestVersionSelector selector = new NewestVersionSelector();
        List<ConflictResolver.ConflictItem> items = new ArrayList<>();

        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.CR1"), 0, 0));
        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Final"), 0, 0));

        ConflictResolver.ConflictContext context = new ConflictResolver.ConflictContext(null, null, null, items);
        selector.selectVersion(context);

        assertThat(context.getWinner().getNode().getVersion().toString()).isEqualTo("1.0.0.Final");
    }

    @Test
    public void differentVersionComparison() throws Exception {
        NewestVersionSelector selector = new NewestVersionSelector();
        List<ConflictResolver.ConflictItem> items = new ArrayList<>();

        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.1.Alpha4"), 0, 0));
        items.add(new ConflictResolver.ConflictItem(null, createNode("1.0.0.Final"), 0, 0));

        ConflictResolver.ConflictContext context = new ConflictResolver.ConflictContext(null, null, null, items);
        selector.selectVersion(context);

        assertThat(context.getWinner().getNode().getVersion().toString()).isEqualTo("1.0.1.Alpha4");
    }

    private DependencyNode createNode(String version) {
        return new DependencyNode() {
            @Override
            public List<DependencyNode> getChildren() {
                return null;
            }

            @Override
            public void setChildren(List<DependencyNode> children) {

            }

            @Override
            public Dependency getDependency() {
                return null;
            }

            @Override
            public Artifact getArtifact() {
                return null;
            }

            @Override
            public void setArtifact(Artifact artifact) {

            }

            @Override
            public List<? extends Artifact> getRelocations() {
                return null;
            }

            @Override
            public Collection<? extends Artifact> getAliases() {
                return null;
            }

            @Override
            public VersionConstraint getVersionConstraint() {
                return TestHelper.versionConstraint(version);
            }

            @Override
            public Version getVersion() {
                return TestHelper.version(version);
            }

            @Override
            public void setScope(String scope) {

            }

            @Override
            public void setOptional(Boolean optional) {

            }

            @Override
            public int getManagedBits() {
                return 0;
            }

            @Override
            public List<RemoteRepository> getRepositories() {
                return null;
            }

            @Override
            public String getRequestContext() {
                return null;
            }

            @Override
            public void setRequestContext(String context) {

            }

            @Override
            public Map<?, ?> getData() {
                return null;
            }

            @Override
            public void setData(Map<Object, Object> data) {

            }

            @Override
            public void setData(Object key, Object value) {

            }

            @Override
            public boolean accept(DependencyVisitor visitor) {
                return false;
            }
        };
    }
}
