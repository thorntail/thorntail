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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.async.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.wildfly.swarm.microprofile.faulttolerance.deployment.async.retry.AsyncHelloService.COUNTER;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.async.retry.AsyncHelloService.Result;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class AsynchronousRetryTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(AsynchronousRetryTest.class).addPackage(AsynchronousRetryTest.class.getPackage());
    }

    @Test
    public void testAsyncRetry(AsyncHelloService helloService) throws IOException, InterruptedException, ExecutionException {
        COUNTER.set(0);
        assertEquals("Hello", helloService.retry(Result.SUCCESS).get());
        assertEquals(1, COUNTER.get());
        COUNTER.set(0);
        try {
            helloService.retry(Result.FAILURE).get();
            fail();
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof IOException);
        }
        assertEquals(3, COUNTER.get());
    }

    @Test
    public void testAsyncRetryFutureCompletesExceptionally(AsyncHelloService helloService) throws IOException, InterruptedException, ExecutionException {
        COUNTER.set(0);
        try {
            helloService.retry(Result.COMPLETE_EXCEPTIONALLY).get();
            fail();
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof IOException);
        }
        assertEquals(1, COUNTER.get());
    }

    @Test
    public void testAsyncRetryFallback(AsyncHelloService helloService) throws IOException, InterruptedException, ExecutionException {
        COUNTER.set(0);
        assertEquals("Fallback", helloService.retryWithFallback(Result.FAILURE).get());
        assertEquals(3, COUNTER.get());
    }

}
