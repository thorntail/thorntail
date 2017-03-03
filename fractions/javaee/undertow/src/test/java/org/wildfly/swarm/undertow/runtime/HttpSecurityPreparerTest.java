package org.wildfly.swarm.undertow.runtime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;
import org.yaml.snakeyaml.Yaml;

import static org.fest.assertions.Assertions.assertThat;


public class HttpSecurityPreparerTest {

    private HttpSecurityPreparer preparer;
    private WARArchive archive;

    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Before
    public void setUp() {
        preparer = new HttpSecurityPreparer();
        archive = ShrinkWrap.create(WARArchive.class, "app.war");
    }

    @Test
    public void do_nothing_if_not_specified_security_constraints() throws Exception {
        preparer.prepareArchive(archive);
        assertThat(archive.get(WebXmlAsset.NAME)).isNull();
    }

    @Test
    public void yaml_parsing() throws Exception {

        InputStream in = getClass().getClassLoader().getResourceAsStream("security.yml");
        assertThat(in).isNotNull().as("security.yml not null");
        Yaml yaml = new Yaml();
        Map<String, Object> httpConfig = (Map<String, Object>) yaml.load(in);

        preparer.deploymentConfigs = (Map)((Map)httpConfig.get("swarm")).get("deployment");
        preparer.prepareArchive(archive);

        try (InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream()) {
            Document document = factory.newDocumentBuilder().parse(assetStream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression xpr = xpath.compile("count(//security-constraint)");
            int securityConstraintsNum = ((Number) xpr.evaluate(document, XPathConstants.NUMBER)).intValue();
            assertThat(securityConstraintsNum).isEqualTo(1);

            xpr = xpath.compile("//url-pattern/text()");
            String urlPattern = (String) xpr.evaluate(document, XPathConstants.STRING);
            assertThat(urlPattern).isEqualTo("/protected");
        }
    }

    @Test
    public void unsupported_auth_method() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> loginConfig = new HashMap<>();
        loginConfig.put("auth-method", "foobar");
        webConfig.put("login-config", loginConfig);

        preparer.deploymentConfigs = deploymentConfig;
        preparer.prepareArchive(archive);

        assertThat(archive.get(WebXmlAsset.NAME)).isNull();
    }

    @Test
    public void set_1_security_constraint() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("url-pattern", "/aaa");

        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));

        preparer.deploymentConfigs = deploymentConfig;
        preparer.prepareArchive(archive);

        try (InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream()) {
            Document document = factory.newDocumentBuilder().parse(assetStream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression xpr = xpath.compile("count(//security-constraint)");
            int securityConstraintsNum = ((Number) xpr.evaluate(document, XPathConstants.NUMBER)).intValue();
            assertThat(securityConstraintsNum).isEqualTo(1);

            xpr = xpath.compile("//url-pattern/text()");
            String urlPattern = (String) xpr.evaluate(document, XPathConstants.STRING);
            assertThat(urlPattern).isEqualTo("/aaa");
        }
    }

    private Map<String, Object> findWebConfig(Map<String, Object> deploymentConfig) {
        String[] path = new String[] {archive.getName(), "web"};
        Map<String, Object> curr = deploymentConfig;

        for (int i=0; i<path.length; i++) {
            curr = (Map<String, Object>)curr.get(path[i]);
        }

        return curr;
    }

    private Map<String, Object> createConfigStub() {
        HashMap<String, Object> deploymentConfig = new HashMap<>();
        HashMap<String, Object> archiveConfig = new HashMap<>();
        HashMap<String, Object> webConfig = new HashMap<>();

        Map<String, Object> loginConfig = new HashMap<>();
        loginConfig.put("auth-method", "BASIC");

        archiveConfig.put("web", webConfig);
        webConfig.put("login-config", loginConfig);
        deploymentConfig.put(archive.getName(), archiveConfig);

        return deploymentConfig;
    }

    @Test
    public void set_2_security_constraints() throws Exception {

        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint1 = new HashMap<>();
        securityConstraint1.put("url-pattern", "/aaa");
        Map<String, Object> securityConstraint2 = new HashMap<>();
        securityConstraint2.put("url-pattern", "/bbb");


        webConfig.put("security-constraints", Arrays.asList(securityConstraint1, securityConstraint2));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.prepareArchive(archive);

        try (InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream()) {
            Document document = factory.newDocumentBuilder().parse(assetStream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression xpr = xpath.compile("count(//security-constraint)");
            Number count = (Number) xpr.evaluate(document, XPathConstants.NUMBER);
            assertThat(count.intValue()).isEqualTo(2);
        }
    }

    @Test
    public void set_1_method() throws Exception {
        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("methods", Arrays.asList("GET"));


        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.prepareArchive(archive);

        try (InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream()) {
            Document document = factory.newDocumentBuilder().parse(assetStream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression xpr  = xpath.compile("count(//http-method)");
            int methodsNum = ((Number) xpr.evaluate(document, XPathConstants.NUMBER)).intValue();
            assertThat(methodsNum).isEqualTo(1);

            xpr = xpath.compile("//http-method/text()");
            String urlPattern = (String) xpr.evaluate(document, XPathConstants.STRING);
            assertThat(urlPattern).isEqualTo("GET");
        }
    }

    @Test
    public void set_2_methods() throws Exception {

        Map<String, Object> deploymentConfig = createConfigStub();
        Map<String, Object> webConfig = findWebConfig(deploymentConfig);

        Map<String, Object> securityConstraint = new HashMap<>();
        securityConstraint.put("methods", Arrays.asList("GET", "POST"));


        webConfig.put("security-constraints", Collections.singletonList(securityConstraint));
        preparer.deploymentConfigs = deploymentConfig;
        preparer.prepareArchive(archive);

        try (InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream()) {
            Document document = factory.newDocumentBuilder().parse(assetStream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            XPathExpression xpr  = xpath.compile("count(//http-method)");
            int methodsNum = ((Number) xpr.evaluate(document, XPathConstants.NUMBER)).intValue();
            assertThat(methodsNum).isEqualTo(2);

            List<String> methods = new ArrayList<>();
            NodeIterator it = XPathAPI.selectNodeIterator(document, "//http-method/text()");
            Node node;
            while ((node = it.nextNode()) != null) {
                methods.add(node.getNodeValue());
            }
            assertThat(methods).contains("GET", "POST");
        }
    }

}
