/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.async.circuitbreaker;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.wildfly.swarm.microprofile.faulttolerance.deployment.async.circuitbreaker.AsyncHelloService.COUNTER;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.async.circuitbreaker.AsyncHelloService.Result;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AsynchronousCircuitBreakerTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(AsynchronousCircuitBreakerTest.class).addPackage(AsynchronousCircuitBreakerTest.class.getPackage());
    }

    @Test
    public void testAsyncCircuitBreaker(AsyncHelloService helloService) throws IOException, InterruptedException, ExecutionException {
        COUNTER.set(0);
        for (int i = 0; i < AsyncHelloService.THRESHOLD; i++) {
            try {
                helloService.hello(Result.FAILURE).get();
                Assert.fail();
            } catch (ExecutionException e) {
                Assert.assertTrue("Unexpected expection on iteration " + i + ": " + e, e.getCause() instanceof IOException);
            }
        }
        // Circuit should be open now
        try {
            helloService.hello(Result.SUCCESS).get();
            Assert.fail();
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof CircuitBreakerOpenException);
        }
        assertEquals(AsyncHelloService.THRESHOLD, COUNTER.get());
        await().atMost(AsyncHelloService.DELAY * 2, TimeUnit.MILLISECONDS).untilAsserted(() -> {
            try {
                assertEquals(AsyncHelloService.OK, helloService.hello(Result.SUCCESS).get());
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof CircuitBreakerOpenException)) {
                    // CircuitBreakerOpenException is expected
                    throw e;
                }
            }
        });
    }

}
