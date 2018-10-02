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
package org.wildfly.swarm.microprofile.restclient;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.concurrent.Callable;

import static org.fest.assertions.Fail.fail;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/26/18
 */
@RunWith(Arquillian.class)
public class ConnectionReleaseTest {

    @ArquillianResource
    URL url;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive testArchive = ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(FailOnEvenRequestsResource.class)
                .addClass(JaxRsActivator.class)
                .addClass(BooleanValue.class)
                .addClass(ConnectionReleaseTest.class);
        return testArchive;
    }

    @Before
    public void reset() {
        Resource client = RestClientBuilder.newBuilder().baseUrl(url).build(Resource.class);
        // clear the state
        client.cleanUp();
    }

    @Test(timeout = 10000L)
    public void shouldReleaseConnectionOnError() {
        Resource client = RestClientBuilder.newBuilder().baseUrl(url).build(Resource.class);
        // this request should be successful
        client.get();

        // this request should fail
        expectFailure(() -> client.get().getContent());

        // this request should be successful
        assertTrue(client.get().getContent());
    }

    @Test(timeout = 10000L)
    public void shouldReleaseConnectionOnErrorOnPooledClient() {
        Resource client = RestClientBuilder.newBuilder()
                .baseUrl(url)
                .property("resteasy.connectionPoolSize", 1)
                .build(Resource.class);
        // this request should be successful
        client.get();

        // this request should fail
        expectFailure(() -> client.get().getContent());

        // this request should be successful
        assertTrue(client.get().getContent());
    }

    private void expectFailure(Callable callable) {
        try {
            // this request should fail
            callable.call();
        } catch (Exception ignored) {
            return;
        }
        fail("The call that was expected to fall succeeded");
    }


    @Path("/v1")
    @Produces(MediaType.APPLICATION_JSON)
    public interface Resource {
        @GET
        BooleanValue get();

        @DELETE
        void cleanUp();
    }
}
