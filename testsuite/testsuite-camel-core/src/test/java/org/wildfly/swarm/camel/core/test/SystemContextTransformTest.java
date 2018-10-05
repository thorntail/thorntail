/*
 * #%L
 * Camel Core :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.swarm.camel.core.test;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.runtime.ServiceLocator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.extension.camel.CamelContextRegistry;
import org.wildfly.swarm.arquillian.DefaultDeployment;


/**
 * Verifiy usage of a system camel context
 *
 * @author thomas.diesler@jboss.com
 * @since 09-Mar-2016
 */
@CamelAware
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
public class SystemContextTransformTest {

    @Test
    public void testSimpleTransform() throws Exception {

        CamelContextRegistry contextRegistry = ServiceLocator.getRequiredService(CamelContextRegistry.class);
        CamelContext camelctx = contextRegistry.getCamelContext("myname");
        Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());

        ProducerTemplate producer = camelctx.createProducerTemplate();
        String result = producer.requestBody("direct:start", "Kermit", String.class);
        Assert.assertEquals("Hello Kermit", result);
    }
}
