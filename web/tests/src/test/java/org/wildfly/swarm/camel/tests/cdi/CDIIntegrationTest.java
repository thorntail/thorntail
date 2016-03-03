/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.camel.tests.cdi;

import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.cdi.Uri;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.util.CamelContextHelper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelContextRegistry;
import org.wildfly.swarm.camel.tests.cdi.subA.Constants;
import org.wildfly.swarm.camel.tests.cdi.subA.RoutesContextA;
import org.wildfly.swarm.camel.tests.cdi.subA.RoutesContextB;
import org.wildfly.swarm.camel.tests.cdi.subA.RoutesContextC;
import org.wildfly.swarm.camel.tests.cdi.subA.RoutesContextD;

@RunWith(Arquillian.class)
public class CDIIntegrationTest {

    @Resource(name = "java:jboss/camel/CamelContextRegistry")
    CamelContextRegistry contextRegistry;

    @Inject
    RoutesContextA routesA;
    @Inject
    RoutesContextB routesB;
    @Inject
    RoutesContextC routesC;
    @Inject
    RoutesContextD routesD;

    @Inject
    @Uri(value = "seda:foo", context = "contextD")
    ProducerTemplate producerD;

    @Deployment(testable = true)
    public static JavaArchive createDeployment() {
        // Note, this needs to have the *.jar suffix
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "camel-cdi-tests.jar");
        archive.addPackage(RoutesContextA.class.getPackage());
        archive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Test
    public void checkContextsHaveCorrectEndpointsAndRoutes() throws Exception {

        CamelContext contextA = assertCamelContext("contextA");
        assertHasEndpoints(contextA, "seda://A.a", "mock://A.b");

        MockEndpoint mockEndpoint = routesA.b;
        mockEndpoint.expectedBodiesReceived(Constants.EXPECTED_BODIES_A);
        routesA.sendMessages();
        mockEndpoint.assertIsSatisfied();

        CamelContext contextB = assertCamelContext("contextB");
        assertHasEndpoints(contextB, "seda://B.a", "mock://B.b");

        MockEndpoint mockEndpointB = routesB.b;
        mockEndpointB.expectedBodiesReceived(Constants.EXPECTED_BODIES_B);
        routesB.sendMessages();
        mockEndpointB.assertIsSatisfied();

        // lets check the routes where we default the context from the @ContextName
        CamelContext contextC = assertCamelContext("contextC");
        assertHasEndpoints(contextC, "seda://C.a", "mock://C.b");

        MockEndpoint mockEndpointC = routesC.b;
        mockEndpointC.expectedBodiesReceived(Constants.EXPECTED_BODIES_C);
        routesC.sendMessages();
        mockEndpointC.assertIsSatisfied();

        CamelContext contextD = assertCamelContext("contextD");
        assertHasEndpoints(contextD, "seda://D.a", "mock://D.b");

        MockEndpoint mockEndpointD = routesD.b;
        mockEndpointD.expectedBodiesReceived(Constants.EXPECTED_BODIES_D);
        routesD.sendMessages();
        mockEndpointD.assertIsSatisfied();

        CamelContext contextE = assertCamelContext("contextD");
        assertHasEndpoints(contextE, "seda://D.a", "mock://D.b");
        MockEndpoint mockDb = CamelContextHelper.getMandatoryEndpoint(contextE, "mock://D.b", MockEndpoint.class);
        mockDb.reset();
        mockDb.expectedBodiesReceived(Constants.EXPECTED_BODIES_D_A);
        for (Object body : Constants.EXPECTED_BODIES_D_A) {
            producerD.sendBody("seda:D.a", body);
        }
        mockDb.assertIsSatisfied();
    }

    private void assertHasEndpoints(CamelContext context, String... uris) {
        Map<String, Endpoint> endpointMap = context.getEndpointMap();
        for (String uri : uris) {
            Endpoint endpoint = endpointMap.get(uri);
            Assert.assertNotNull("CamelContext " + context + " does not have an Endpoint with URI " + uri + " but has " + endpointMap.keySet(), endpoint);
        }
    }

    private CamelContext assertCamelContext(String contextName) {
        CamelContext camelctx = contextRegistry.getCamelContext(contextName);
        Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());
        return camelctx;
    }
}
