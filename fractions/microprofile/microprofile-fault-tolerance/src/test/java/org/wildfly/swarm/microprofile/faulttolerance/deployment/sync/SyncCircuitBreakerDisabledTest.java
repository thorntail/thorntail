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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.sync;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.HystrixCommandInterceptor;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class SyncCircuitBreakerDisabledTest {

    @Deployment
    public static JavaArchive createTestArchive() throws NoSuchMethodException, SecurityException {
        return TestArchive.createBase("SyncCircuitBreakerDisabledTest.jar")
                .addPackage(SyncCircuitBreakerDisabledTest.class.getPackage())
                .addAsManifestResource(new StringAsset(HystrixCommandInterceptor.SYNC_CIRCUIT_BREAKER_KEY + "=false"), "microprofile-config.properties");
    }

    @Inject
    ShakyServiceClient client;

    @Test
    public void testDefaultHystrixCircuitBreakerUsed() throws InterruptedException {
        // Verify Hystrix config first
        DynamicLongProperty intervalInMilliseconds = DynamicPropertyFactory.getInstance()
                .getLongProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", 500);
        assertEquals(intervalInMilliseconds.get(), 10);

        // CLOSED
        for (int i = 0; i < ShakyServiceClient.REQUEST_THRESHOLD; i++) {
            assertInvocation(false);
        }
        // Should be OPEN now
        HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey(getCommandKey()));
        assertNotNull(breaker);
        assertFalse(breaker.getClass().getName().contains("org.wildfly.swarm.microprofile.faulttolerance"));
        assertTrue(breaker.isOpen());
        assertInvocation(true);
        TimeUnit.MILLISECONDS.sleep(ShakyServiceClient.DELAY);
        // Should be HALF-OPEN
        assertInvocation(false);
        // OPEN again
        assertInvocation(true);
    }

    private String getCommandKey() {
        try {
            return ShakyServiceClient.class.getDeclaredMethod("ping").toGenericString();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException();
        }
    }

    private void assertInvocation(boolean open) throws InterruptedException {
        try {
            client.ping();
            fail("Invocation should always fail!");
        } catch (Exception e) {
            if (open) {
                assertTrue("Circuit breaker must be open: " + e, e instanceof CircuitBreakerOpenException);
            } else {
                assertTrue("IllegalStateException expected: " + e, e instanceof IllegalStateException);
            }
        }
        TimeUnit.MILLISECONDS.sleep(100);
    }
}
