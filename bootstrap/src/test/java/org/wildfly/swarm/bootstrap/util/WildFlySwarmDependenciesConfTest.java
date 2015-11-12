package org.wildfly.swarm.bootstrap.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmDependenciesConfTest {

    @Test
    public void testRoundTrip() throws Exception {

        WildFlySwarmDependenciesConf conf = new WildFlySwarmDependenciesConf();

        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final"));
        conf.addPrimaryDependency(MavenArtifactDescriptor.fromMscGav("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final"));
        conf.addExtraDependency(MavenArtifactDescriptor.fromMavenGav("org.jolokia:jolokia-war:war:1.3.2"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        conf.write(out);

        String written = new String(out.toByteArray());

        String[] lines = written.split("\n");

        assertThat(lines).hasSize(3);

        assertThat(lines).contains("primary:org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final");
        assertThat(lines).contains("primary:org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final");
        assertThat(lines).contains("extra:org.jolokia:jolokia-war:war:1.3.2");

        ByteArrayInputStream in = new ByteArrayInputStream(written.getBytes());

        conf = new WildFlySwarmDependenciesConf(in);

        assertThat(conf.getPrimaryDependencies()).hasSize(2);

        List<String> primaryGavs = conf.getPrimaryDependencies().stream().map(e -> e.mscGav()).collect(Collectors.toList());

        assertThat(primaryGavs).contains("org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.0.Final");
        assertThat(primaryGavs).contains("org.jboss.spec.javax.servlet:jboss-servlet-api_3.1_spec:1.0.0.Final");

        assertThat(conf.getExtraDependencies()).hasSize(1);

        List<String> extraGavs = conf.getExtraDependencies().stream().map(e -> e.mavenGav()).collect(Collectors.toList());
        assertThat(extraGavs).contains("org.jolokia:jolokia-war:war:1.3.2");
    }
}
