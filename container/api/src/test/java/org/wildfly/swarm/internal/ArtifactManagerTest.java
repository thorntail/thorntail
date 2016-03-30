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
package org.wildfly.swarm.internal;

import java.util.List;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ArtifactManagerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {

        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.wildfly.swarm:jaxrs:1.0.0.Beta4"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.wildfly.swarm:weld:1.0.0.Beta4"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMavenGav("joda-time:joda-time:2.7"));
        conf.addExtraDependency(MavenArtifactDescriptor.fromMavenGav("org.jolokia:jolokia-war:war:1.3.2"));
    }

    @Before
    public void setUp() {

        manager = new ArtifactManager(conf);
    }

    @Test
    public void testDetermineVersion() throws Exception {

        assertThat(manager.determineVersionViaDependenciesConf("org.jboss.spec.javax.enterprise.concurrent", "jboss-concurrency-api_1.0_spec", "jar", null))
                .isEqualTo("1.0.0.Final");
        assertThat(manager.determineVersionViaDependenciesConf("org.jolokia", "jolokia-war", "war", null))
                .isEqualTo("1.3.2");
        assertThat(manager.determineVersionViaDependenciesConf(ARTIFACT_INVALID_GROUP_ID, ARTIFACT_INVALID_ARTIFACT_ID, ARTIFACT_INVALID_PACKAGING, null))
                .isNull();

    }

    @Test
    public void testArtifactGroupArtifact() throws Exception {

        assertThat(manager.artifact(String.format("%s:%s", ARTIFACT_GROUP_ID, ARTIFACT_ARTIFACT_ID)).getName())
                .isEqualTo(ARTIFACT_NAME);
    }

    @Test
    public void testArtifactGroupArtifactExplicitName() throws Exception {

        String name = "myname";

        assertThat(manager.artifact(String.format("%s:%s", ARTIFACT_GROUP_ID, ARTIFACT_ARTIFACT_ID), name).getName())
                .isEqualTo(name);
    }

    @Test
    public void testArtifactGroupArtifactVersion() throws Exception {

        assertThat(manager.artifact(String.format("%s:%s:%s", ARTIFACT_GROUP_ID, ARTIFACT_ARTIFACT_ID, ARTIFACT_VERSION)).getName())
                .isEqualTo(ARTIFACT_NAME);
    }

    @Test
    public void testArtifactGroupArtifactPackagingVersion() throws Exception {

        assertThat(manager.artifact(String.format("%s:%s:%s:%s", ARTIFACT_GROUP_ID, ARTIFACT_ARTIFACT_ID, ARTIFACT_PACKAGING, ARTIFACT_VERSION))
                           .getName()).isEqualTo(ARTIFACT_NAME);
    }

    @Test
    public void testArtifactInvalidGroupArtifact() throws Exception {

        try {
            manager.artifact(String.format("%s:%s", ARTIFACT_INVALID_GROUP_ID, ARTIFACT_INVALID_ARTIFACT_ID));
            fail("RuntimeException should have been thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Unable to determine version number from GAV");
        }
    }

    @Test
    public void testArtifactInvalidGroupArtifactVersion() throws Exception {

        try {
            manager.artifact(String.format("%s:%s:%s", ARTIFACT_INVALID_GROUP_ID, ARTIFACT_INVALID_ARTIFACT_ID, ARTIFACT_INVALID_VERSION));
            fail("RuntimeException should have been thrown");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Artifact not found.");
        }
    }

    @Test
    public void testArtifactInvalidGroupArtifactPackagingVersion() throws Exception {

        try {
            manager.artifact(String.format("%s:%s:%s:%s", ARTIFACT_INVALID_GROUP_ID, ARTIFACT_INVALID_ARTIFACT_ID, ARTIFACT_INVALID_PACKAGING,
                                           ARTIFACT_INVALID_VERSION));
            fail("RuntimeException should have been thrown");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Artifact not found.");
        }
    }

    @Test
    public void wildflySwarmDependenciesExcluded() throws Exception {
        List<JavaArchive> archives = manager.allArtifacts("org.wildfly.swarm");
        assertThat(archives.size()).isEqualTo(3);
    }

    @Test
    public void noDependenciesExcluded() throws Exception {
        List<JavaArchive> archives = manager.allArtifacts();
        assertThat(archives.size()).isEqualTo(5);
    }

    private static WildFlySwarmDependenciesConf conf = new WildFlySwarmDependenciesConf();

    private static String ARTIFACT_GROUP_ID = "org.jboss.spec.javax.servlet";

    private static String ARTIFACT_ARTIFACT_ID = "jboss-servlet-api_3.1_spec";

    private static String ARTIFACT_PACKAGING = "jar";

    private static String ARTIFACT_VERSION = "1.0.0.Final";

    private static String ARTIFACT_NAME = "jboss-servlet-api_3.1_spec-1.0.0.Final.jar";

    private static String ARTIFACT_INVALID_GROUP_ID = "no.such";

    private static String ARTIFACT_INVALID_ARTIFACT_ID = "thingy";

    private static String ARTIFACT_INVALID_PACKAGING = "jar";

    private static String ARTIFACT_INVALID_VERSION = "1.0.0";

    private ArtifactManager manager;

}
