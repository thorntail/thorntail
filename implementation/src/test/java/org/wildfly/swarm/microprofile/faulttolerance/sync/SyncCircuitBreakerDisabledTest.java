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
package org.wildfly.swarm.microprofile.faulttolerance.sync;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.faulttolerance.HystrixCommandInterceptor;
import org.wildfly.swarm.microprofile.faulttolerance.HystrixExtension;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;

/**
 *
 * @author Martin Kouba
 */
public class SyncCircuitBreakerDisabledTest extends Arquillian {

    @Deployment
    public static JavaArchive createTestArchive() throws NoSuchMethodException, SecurityException {
        return ShrinkWrap.create(JavaArchive.class).addPackage(SyncCircuitBreakerDisabledTest.class.getPackage()).addClass(HystrixCommandInterceptor.class)
                .addAsManifestResource(new StringAsset(HystrixCommandInterceptor.SYNC_CIRCUIT_BREAKER_KEY + "=false"), "microprofile-config.properties")
                .addAsServiceProvider(Extension.class, HystrixExtension.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
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
                assertTrue(e instanceof CircuitBreakerOpenException, "Circuit breaker must be open: " + e);
            } else {
                assertTrue(e instanceof IllegalStateException, "IllegalStateException expected: " + e);
            }
        }
        TimeUnit.MILLISECONDS.sleep(100);
    }
}
