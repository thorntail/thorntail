package org.wildfly.swarm.container.runtime;

import junit.framework.Assert;
import org.jboss.dmr.ModelNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.List;

/**
 * @author Heiko Braun
 * @since 26/11/15
 */
public class ParserTestCase {

    private static URL xml;

    @BeforeClass
    public static void init() {
        ClassLoader cl = ParserTestCase.class.getClassLoader();
        xml = cl.getResource("standalone.xml");
    }

    @Test
    public void testDelegatingParser() throws Exception {
        StandaloneXmlParser parser = new StandaloneXmlParser();
        List<ModelNode> operations = parser.parse(xml);
        Assert.assertEquals(28, operations.size());
    }
}
