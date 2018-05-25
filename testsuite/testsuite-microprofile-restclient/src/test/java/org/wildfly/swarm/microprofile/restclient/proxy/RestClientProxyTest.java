/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.restclient.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.restclient.Counter;
import org.wildfly.swarm.microprofile.restclient.HelloResource;
import org.wildfly.swarm.microprofile.restclient.RestClientProxy;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class RestClientProxyTest {

    @Inject
    Counter counter;

    @ArquillianResource
    URL url;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addPackage(HelloResource.class.getPackage())
                .addPackage(RestClientProxyTest.class.getPackage());
    }

    @Test
    public void testGetClient() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);

        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        Client client = ((RestClientProxy) helloClient).getClient();
        assertNotNull(client);
        assertEquals("C:OK1:C", helloClient.hello());
        client.close();
    }

    @Test
    public void testClose() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);

        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        RestClientProxy clientProxy = (RestClientProxy) helloClient;
        assertEquals("C:OK1:C", helloClient.hello());
        clientProxy.close();
        assertTrue(CharlieService.DESTROYED.get());
        // Further calls are no-op
        clientProxy.close();
        // Invoking any method on the client proxy should result in ISE
        try {
            helloClient.hello();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

}