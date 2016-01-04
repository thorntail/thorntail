package org.wildfly.swarm;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ArtifactManagerTest {

    private static WildFlySwarmDependenciesConf conf = new WildFlySwarmDependenciesConf();
    private static String ARTIFACT_GROUP_ID="org.jboss.spec.javax.servlet";
    private static String ARTIFACT_ARTIFACT_ID="jboss-servlet-api_3.1_spec";
    private static String ARTIFACT_PACKAGING="jar";
    private static String ARTIFACT_VERSION="1.0.0.Final";
    private static String ARTIFACT_NAME="jboss-servlet-api_3.1_spec-1.0.0.Final.jar";
    private static String ARTIFACT_INVALID_GROUP_ID="no.such";
    private static String ARTIFACT_INVALID_ARTIFACT_ID="thingy";
    private static String ARTIFACT_INVALID_PACKAGING="jar";
    private static String ARTIFACT_INVALID_VERSION="1.0.0";

    private ArtifactManager manager;

    @BeforeClass
    public static void setUpClass() throws Exception {

        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final"));
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
        assertThat(manager.determineVersionViaDependenciesConf(ARTIFACT_INVALID_GROUP_ID, ARTIFACT_INVALID_ARTIFACT_ID, ARTIFACT_INVALID_PACKAGING, null ) )
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
            assertThat(e.getMessage()).contains( "Unable to determine version number from GAV" );
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

}
