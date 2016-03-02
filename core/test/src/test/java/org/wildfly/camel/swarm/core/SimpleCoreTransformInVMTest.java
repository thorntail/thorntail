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
package org.wildfly.camel.swarm.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.JARArchive;


/**
 * @author thomas.diesler@jboss.com
 * @since 09-Feb-2016
 */
@Ignore("[#1059] Investigate camel-core swarm in VM startup failure")
public class SimpleCoreTransformInVMTest {

    @Test
    public void testSimpleTransform() throws Exception {
        Container container = new Container().fraction(new CamelCoreFraction()).start(camelDeployment());
        try {
            CamelContext camelctx = new DefaultCamelContext();
            camelctx.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    Path path = Paths.get(System.getProperty("java.io.tmpdir"), "fileA");
                    from("file://" + path.getParent() + "?fileName=" + path.getFileName()).to("direct:end");
                }
            });

            camelctx.start();
            try {
                ConsumerTemplate consumer = camelctx.createConsumerTemplate();
                String result = consumer.receiveBody("direct:end", String.class);
                Assert.assertEquals("Hello 1", result);
            } finally {
                camelctx.stop();
            }
        } finally {
            container.stop();
        }
    }

    public JARArchive camelDeployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.addAsResource("spring/simple-camel-context.xml");
        return archive;
    }
}
