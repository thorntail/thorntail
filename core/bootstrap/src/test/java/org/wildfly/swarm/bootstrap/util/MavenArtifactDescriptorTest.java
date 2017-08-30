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
package org.wildfly.swarm.bootstrap.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class MavenArtifactDescriptorTest {

    @Test
    public void testEquals() {
        MavenArtifactDescriptor left = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        MavenArtifactDescriptor right = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        assertThat(left).isEqualTo(right);
    }

    @Test
    public void testCollectionContains() {

        MavenArtifactDescriptor left = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        MavenArtifactDescriptor right = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        List<MavenArtifactDescriptor> list = new ArrayList<>();

        list.add(left);

        assertThat(list).contains(right);
        assertThat(list.contains(right)).isTrue();

    }

    @Test
    public void testMscGav() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        assertThat(desc.mscGav()).isEqualTo("org.wildfly.swarm:fish:1.0");
    }

    @Test
    public void testMscGavWithClassifier() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0")
                .classifier("redhat-1").build();

        assertThat(desc.mscGav()).isEqualTo("org.wildfly.swarm:fish:1.0:redhat-1");
    }

    @Test
    public void testMavenGav() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        assertThat(desc.mavenGav()).isEqualTo("org.wildfly.swarm:fish:jar:1.0");
    }

    @Test
    public void testMavenGavWithClassifier() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0")
                .classifier("redhat-1").build();

        assertThat(desc.mavenGav()).isEqualTo("org.wildfly.swarm:fish:jar:redhat-1:1.0");
    }

    @Test
    public void testRepoPath() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0").build();

        assertThat(desc.repoPath(true)).isEqualTo("org/wildfly/swarm/fish/1.0/fish-1.0.jar");
    }

    @Test
    public void testRepoPathWithClassifier() {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.build()
                .groupId("org.wildfly.swarm")
                .artifactId("fish")
                .version("1.0")
                .classifier("redhat-1").build();

        assertThat(desc.repoPath(true)).isEqualTo("org/wildfly/swarm/fish/1.0/fish-1.0-redhat-1.jar");
    }

    @Test
    public void testFromMscGav() throws IOException {
        MavenArtifactDescriptor desc = MavenArtifactDescriptor.fromMscGav("org.wildfly.swarm:container:1.0");

        assertThat(desc.groupId()).isEqualTo("org.wildfly.swarm");
        assertThat(desc.artifactId()).isEqualTo("container");
        assertThat(desc.version()).isEqualTo("1.0");
        assertThat(desc.type()).isEqualTo("jar");
        assertThat(desc.classifier()).isNull();

        assertThat(desc.mscGav()).isEqualTo("org.wildfly.swarm:container:1.0");
    }

}
