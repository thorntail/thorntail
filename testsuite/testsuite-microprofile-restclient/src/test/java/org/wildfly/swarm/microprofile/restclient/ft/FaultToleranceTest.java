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
package org.wildfly.swarm.microprofile.restclient.ft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
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
import org.wildfly.swarm.microprofile.restclient.Latch;
import org.wildfly.swarm.microprofile.restclient.Timer;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class FaultToleranceTest {

    static final int BULKHEAD = 2;

    @Inject
    Counter counter;

    @Inject
    Timer timer;

    @Inject
    Latch latch;

    @ArquillianResource
    URL url;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addPackage(HelloResource.class.getPackage())
                .addPackage(FaultToleranceTest.class.getPackage());
    }

    @Test
    public void testRetry() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        counter.reset(3);
        timer.reset(0);

        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        assertEquals("OK3", helloClient.helloRetry());
    }

    @Test
    public void testFallback() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        timer.reset(0);
        counter.reset(3);

        assertEquals("fallback", helloClient.helloFallback());
        assertEquals("defaultFallback", helloClient.helloFallbackDefaultMethod());
    }

    @Test
    public void testCircuitBreaker() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        counter.reset(3);
        timer.reset(0);

        try {
            helloClient.helloCircuitBreaker();
            fail();
        } catch (WebApplicationException expected) {
        }
        try {
            helloClient.helloCircuitBreaker();
            fail();
        } catch (WebApplicationException expected) {
        }
        try {
            helloClient.helloCircuitBreaker();
            fail();
        } catch (CircuitBreakerOpenException expected) {
        }
    }

    @Test
    public void testCircuitBreakerClassLevel() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        HelloClientClassLevelCircuitBreaker client = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClientClassLevelCircuitBreaker.class);

        counter.reset(3);
        timer.reset(0);

        try {
            client.helloCircuitBreaker();
            fail();
        } catch (WebApplicationException expected) {
        }
        try {
            client.helloCircuitBreaker();
            fail();
        } catch (WebApplicationException expected) {
        }
        try {
            client.helloCircuitBreaker();
            fail();
        } catch (CircuitBreakerOpenException expected) {
        }
    }

    @Test
    public void testTimeout() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException {
        HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);

        counter.reset(1);
        timer.reset(400);
        latch.reset(1);

        try {
            helloClient.helloTimeout();
            fail();
        } catch (TimeoutException expected) {
        }

        latch.await();
    }

    @Test
    public void testBulkhead() throws InterruptedException, IllegalStateException, RestClientDefinitionException, MalformedURLException, ExecutionException {

        int pool = BULKHEAD;
        // Start latch
        latch.reset(pool);
        // End latch
        latch.add("end", 1);

        ExecutorService executor = Executors.newFixedThreadPool(pool);

        try {
            HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).property("resteasy.connectionPoolSize", pool * 2).build(HelloClient.class);

            List<Future<String>>  results = new ArrayList<>();
            for (int i = 0; i < pool; i++) {
                results.add(executor.submit(() -> helloClient.helloBulkhead(true)));
            }
            // Wait until all requests are being processed
            if(!latch.await()) {
                fail();
            }

            // Next invocation should return fallback due to BulkheadException
            assertEquals("bulkheadFallback", helloClient.helloBulkhead(false));

            latch.countDown("end");
            for (Future<String> future : results) {
                assertEquals("OK", future.get());
            }

        } finally {
            executor.shutdown();
        }
    }

}