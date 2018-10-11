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

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         <br>
 *         Date: 5/15/17
 */
public class ExtraArtifactsHandlerTest {

    public static final String GROUP_ID = "org.wildfly.swarm";
    public static final String ARTIFACT_ID = "wildfly-swarm";
    public static final String JAR = "jar";
    public static final String VERSION = "2017.5.0";

    @Before
    public void setUp() {
        System.clearProperty("thorntail.download.poms");
        System.clearProperty("thorntail.download.sources");
        System.clearProperty("thorntail.download.javadocs");
    }

    @Test
    public void shouldGetOnlyPomWhenPomsSpecified() throws Exception {
        System.setProperty("thorntail.download.poms", "");

        shouldGetWithClassifierAndExtension("", "pom");
    }

    @Test
    public void shouldGetOnlySourcesWhenSourcesSpecified() throws Exception {
        System.setProperty("thorntail.download.sources", "");
        shouldGetWithClassifierAndExtension("sources", "jar");
    }

    @Test
    public void shouldGetOnlyJavadocWhenJavadocSpecified() throws Exception {
        System.setProperty("thorntail.download.javadocs", "");
        shouldGetWithClassifierAndExtension("javadoc", "jar");
    }

    private void shouldGetWithClassifierAndExtension(String classifier, String extension) {
        DependencyNode dependency = dependencyNode();
        List<DependencyNode> extraDependencies =
                ExtraArtifactsHandler.getExtraDependencies(Collections.singletonList(dependency));

        assertThat(extraDependencies).hasSize(1);

        DependencyNode extraDependency = extraDependencies.get(0);
        Artifact artifact = extraDependency.getDependency().getArtifact();
        assertThat(artifact.getGroupId()).isEqualTo(GROUP_ID);
        assertThat(artifact.getArtifactId()).isEqualTo(ARTIFACT_ID);
        assertThat(artifact.getVersion()).isEqualTo(VERSION);

        assertThat(artifact.getClassifier()).isEqualTo(classifier);
        assertThat(artifact.getExtension()).isEqualTo(extension);
    }


    private DependencyNode dependencyNode() {        
        DefaultArtifact artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, JAR, VERSION);
        return new DefaultDependencyNode(
                new Dependency(artifact, "system")
        );
    }

}