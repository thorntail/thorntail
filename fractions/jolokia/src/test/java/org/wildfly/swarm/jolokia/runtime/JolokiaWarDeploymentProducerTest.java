package org.wildfly.swarm.jolokia.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Test;
import org.wildfly.swarm.jolokia.JolokiaFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JolokiaWarDeploymentProducerTest {

    @Test
    public void testNoJolokiaAccessAtAll() throws Exception {
        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction();
        producer.lookup = new MockArtifactLookup();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNull();
    }

    @Test
    public void testJolokiaAccessViaUrlOnFraction() throws Exception {
        URL resource = getClass().getClassLoader().getResource("my-jolokia-access.xml");

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccessXml(resource));
        producer.lookup = new MockArtifactLookup();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access.xml");
        }
    }

    @Test
    public void testJolokiaAccessViaFileOnFraction() throws Exception {
        URL resource = getClass().getClassLoader().getResource("my-jolokia-access2.xml");

        String path = resource.getPath();

        File file = new File(path);

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccessXml(file));

        producer.lookup = new MockArtifactLookup();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access2.xml");
        }
    }

    @Test
    public void testJolokiaAccessViaAPI() throws Exception {

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccess(access -> {
                    access.host("1.1.1.1");
                }));

        producer.lookup = new MockArtifactLookup();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().map(String::trim).collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines).contains("<host>1.1.1.1</host>");
        }
    }

    @Test
    public void testPreferConfigValueFile_vs_API() throws Exception {

        URL resource = getClass().getClassLoader().getResource("my-jolokia-access2.xml");

        String path = resource.getPath();

        File file = new File(path);

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccess(access -> {
                    access.host("1.1.1.1");
                }));

        producer.lookup = new MockArtifactLookup();

        producer.jolokiaAccessXML = file.getAbsolutePath();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access2.xml");
        }

    }

    @Test
    public void testPreferConfigValueURL_vs_API() throws Exception {

        URL resource = getClass().getClassLoader().getResource("my-jolokia-access2.xml");

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccess(access -> {
                    access.host("1.1.1.1");
                }));

        producer.lookup = new MockArtifactLookup();

        producer.jolokiaAccessXML = resource.toExternalForm();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access2.xml");
        }

    }

    public void testPreferConfigValueFile_vs_FractionSetting() throws Exception {

        URL resource = getClass().getClassLoader().getResource("my-jolokia-access2.xml");
        URL fractionResource = getClass().getClassLoader().getResource("my-jolokia-access3.xml");

        String path = resource.getPath();

        File file = new File(path);

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccessXml(fractionResource));

        producer.lookup = new MockArtifactLookup();

        producer.jolokiaAccessXML = file.getAbsolutePath();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access2.xml");
        }

    }

    @Test
    public void testPreferConfigValueURL_vs_FractionSetting() throws Exception {

        URL resource = getClass().getClassLoader().getResource("my-jolokia-access2.xml");
        URL fractionResource = getClass().getClassLoader().getResource("my-jolokia-access3.xml");

        JolokiaWarDeploymentProducer producer = new JolokiaWarDeploymentProducer();

        producer.fraction = new JolokiaFraction()
                .prepareJolokiaWar(JolokiaFraction.jolokiaAccessXml(fractionResource));

        producer.lookup = new MockArtifactLookup();

        producer.jolokiaAccessXML = resource.toExternalForm();

        Archive war = producer.jolokiaWar();

        Node xml = war.get("WEB-INF/classes/jolokia-access.xml");

        assertThat(xml).isNotNull();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(xml.getAsset().openStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertThat(lines).isNotEmpty();
            assertThat(lines.get(0)).contains("This is my-jolokia-access2.xml");
        }

    }
}
