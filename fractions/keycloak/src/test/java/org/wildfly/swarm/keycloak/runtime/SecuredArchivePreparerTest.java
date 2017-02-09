package org.wildfly.swarm.keycloak.runtime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.wildfly.swarm.keycloak.KeycloakFraction;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

import static org.fest.assertions.Assertions.assertThat;

public class SecuredArchivePreparerTest {

    private SecuredArchivePreparer preparer;
    private WARArchive archive;

    private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Before
    public void setUp() {
        preparer = new SecuredArchivePreparer();
        preparer.keycloakFraction = new KeycloakFraction();
        archive = ShrinkWrap.create(WARArchive.class);
    }

    @Test
    public void do_nothing_if_not_specified_security_constraints() throws Exception {
        preparer.prepareArchive(archive);

        assertThat(archive.get(WebXmlAsset.NAME)).isNull();
    }

    @Test
    public void set_1_security_constraint() throws Exception {
        preparer.keycloakFraction.securityConstraints(Collections.singletonList("{url-pattern=/aaa}"));
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

    @Test
    public void set_2_security_constraints() throws Exception {
        preparer.keycloakFraction.securityConstraints(Arrays.asList("{url-pattern=/aaa}", "{url-pattern=/bbb"));
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
        preparer.keycloakFraction.securityConstraints(Collections.singletonList("{methods=[GET]}"));
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
        preparer.keycloakFraction.securityConstraints(Collections.singletonList("{methods=[GET, POST]}"));
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