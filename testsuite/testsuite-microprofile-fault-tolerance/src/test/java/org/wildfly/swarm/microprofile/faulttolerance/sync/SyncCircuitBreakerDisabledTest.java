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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;

import io.smallrye.faulttolerance.SimpleCommand;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class SyncCircuitBreakerDisabledTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new FileAsset(new File("src/test/resources/jboss-deployment-structure.xml")), "jboss-deployment-structure.xml")
                .addAsResource(new FileAsset(new File("src/test/resources/project-defaults.yml")), "project-defaults.yml")
                .addPackage(SyncCircuitBreakerDisabledTest.class.getPackage());
    }

    @Inject
    ShakyServiceClient client;

    @Test
    public void testDefaultHystrixCircuitBreakerUsed() throws InterruptedException {
        // Verify Hystrix config first
        DynamicLongProperty intervalInMilliseconds = DynamicPropertyFactory.getInstance()
                .getLongProperty("hystrix.command.default.metrics.healthSnapshot.intervalInMilliseconds", 500);
        assertEquals(intervalInMilliseconds.get(), 10);
        ShakyServiceClient.COUNTER.set(0);

        // CLOSED
        for (int i = 0; i < ShakyServiceClient.REQUEST_THRESHOLD; i++) {
            assertInvocation(false);
        }
        assertEquals(ShakyServiceClient.REQUEST_THRESHOLD, ShakyServiceClient.COUNTER.get());
        // Should be OPEN now
        HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey(getCommandKey()));
        assertNotNull(breaker);
        assertFalse(breaker.getClass().getName().contains("org.wildfly.swarm.microprofile.faulttolerance"));
        assertTrue(breaker.isOpen());
        assertInvocation(true);
        assertEquals(ShakyServiceClient.REQUEST_THRESHOLD, ShakyServiceClient.COUNTER.get());

        // Wait a little so that hystrix allows us to close
        TimeUnit.MILLISECONDS.sleep(ShakyServiceClient.DELAY);

        // Should be HALF-OPEN
        assertInvocation(false, true);
        assertEquals(ShakyServiceClient.REQUEST_THRESHOLD + 1, ShakyServiceClient.COUNTER.get());

        // Should be CLOSED
        assertInvocation(false);
        assertInvocation(false);
        assertEquals(ShakyServiceClient.REQUEST_THRESHOLD + 3, ShakyServiceClient.COUNTER.get());
    }

    private String getCommandKey() {
        try {
            return SimpleCommand.getCommandKey(ShakyServiceClient.class.getDeclaredMethod("ping", Boolean.TYPE));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertInvocation(boolean open) throws InterruptedException {
        assertInvocation(open, false);
    }

    private void assertInvocation(boolean open, boolean success) throws InterruptedException {
        try {
            client.ping(success);
            if (!success) {
                fail("Invocation should have failed!");
            }
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
