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
package org.wildfly.swarm.container.runtime;

import java.net.URL;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.swarm.container.runtime.internal.xmlconfig.StandaloneXMLParser;

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
        StandaloneXMLParser parser = new StandaloneXMLParser();
        List<ModelNode> operations = parser.parse(xml);
        Assert.assertEquals(28, operations.size());
    }
}
