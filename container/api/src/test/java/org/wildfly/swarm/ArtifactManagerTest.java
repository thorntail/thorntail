package org.wildfly.swarm;

import org.junit.Test;
import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmDependenciesConf;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ArtifactManagerTest {

    @Test
    public void testDetermineVersion() throws Exception {

        WildFlySwarmDependenciesConf conf = new WildFlySwarmDependenciesConf();

        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final"));
        conf.addExtraDependency(MavenArtifactDescriptor.fromMavenGav("org.jolokia:jolokia-war:war:1.3.2"));

        ArtifactManager manager = new ArtifactManager(conf);

        assertThat(manager.determineVersionViaDependenciesConf("org.jboss.spec.javax.enterprise.concurrent", "jboss-concurrency-api_1.0_spec", "jar", null))
                .isEqualTo("1.0.0.Final");
        assertThat(manager.determineVersionViaDependenciesConf("org.jolokia", "jolokia-war", "war", null))
                .isEqualTo("1.3.2");
        assertThat(manager.determineVersionViaDependenciesConf( "no.such", "thingy", "jar", null ) )
                .isNull();

    }
}
