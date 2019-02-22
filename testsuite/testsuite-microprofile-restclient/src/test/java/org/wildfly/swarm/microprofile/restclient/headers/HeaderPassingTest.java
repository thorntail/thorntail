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
package org.wildfly.swarm.microprofile.restclient.headers;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.restclient.JaxRsActivator;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.wildfly.swarm.microprofile.restclient.headers.HeaderConsumingResource.HEADER_NAME;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 2/20/19
 */
@RunWith(Arquillian.class)
public class HeaderPassingTest {
    @ArquillianResource
    URL url;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(
                        new StringAsset("org.eclipse.microprofile.rest.client.propagateHeaders=" + HEADER_NAME),
                        "microprofile-config.properties"
                )
                .addClass(JaxRsActivator.class)
                .addPackage(AllHeadersReturningResource.class.getPackage())
                .addPackage(HeaderPassingTest.class.getPackage());
    }

    @Test
    public void shouldPassHeadersFromClientAutomatically() throws IllegalStateException, RestClientDefinitionException {
        String baseUrl = url.toString() + "/v1";
        String headerValue = "some header value";

        HeaderConsumingClient client =
                RestClientBuilder.newBuilder()
                        .baseUri(URI.create(baseUrl))
                        .build(HeaderConsumingClient.class);

        Map<String, String> headers = client.postWithHeader(headerValue, baseUrl);
        assertNotNull(headers);
        assertEquals(headerValue, headers.get(HEADER_NAME));
    }

}
