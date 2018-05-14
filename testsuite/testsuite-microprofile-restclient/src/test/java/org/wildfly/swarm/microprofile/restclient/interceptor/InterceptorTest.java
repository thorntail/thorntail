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
package org.wildfly.swarm.microprofile.restclient.interceptor;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

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

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class InterceptorTest {

    @Inject
    Counter counter;

    @ArquillianResource
    URL url;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addPackage(HelloResource.class.getPackage())
                .addPackage(InterceptorTest.class.getPackage());
    }

    @Test
    public void testInterception() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(1);

        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        // Interceptor ordering should be: Charlie, Alpha, Bravo
        assertEquals("C:A:B:OK1:B:A:C", helloClient.hello());
    }

}