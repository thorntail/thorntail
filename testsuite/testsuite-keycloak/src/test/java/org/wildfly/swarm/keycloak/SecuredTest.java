package org.wildfly.swarm.keycloak;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class SecuredTest {

    @Test
    public void testShouldContainOneLoginConfig() throws Exception {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "test.jar");
        archive.as(Secured.class).protect("/companies").withRole("default");
        archive.as(Secured.class).protect("/accounts").withRole("manager");

        InputStream assetStream = archive.get(WebXmlAsset.NAME).getAsset().openStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = factory.newDocumentBuilder().parse(assetStream);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression xpr = xpath.compile("count(//web-app/login-config)");
        Number count = (Number) xpr.evaluate(document, XPathConstants.NUMBER);
        Assert.assertEquals("Should have only one login-config element", 1, count.intValue());
    }
}
