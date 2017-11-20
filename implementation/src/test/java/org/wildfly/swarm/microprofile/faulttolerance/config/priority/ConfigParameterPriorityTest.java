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
package org.wildfly.swarm.microprofile.faulttolerance.config.priority;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.faulttolerance.FaultToleranceOperations;
import org.wildfly.swarm.microprofile.faulttolerance.TestArchive;
import org.wildfly.swarm.microprofile.faulttolerance.config.FaultToleranceOperation;
import org.wildfly.swarm.microprofile.faulttolerance.config.RetryConfig;

/**
 *
 * @author Martin Kouba
 */
public class ConfigParameterPriorityTest extends Arquillian {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase().addPackage(ConfigParameterPriorityTest.class.getPackage()).addClass(FaultToleranceOperations.class)
                .addAsManifestResource(new StringAsset("Retry/delay=10"), "microprofile-config.properties");
    }

    @Inject
    FaultToleranceOperations ops;

    @Test
    public void testConfig() throws NoSuchMethodException, SecurityException {
        FaultToleranceOperation foo = ops.get(FaultyService.class.getMethod("foo").toGenericString());
        assertNotNull(foo);
        assertTrue(foo.hasRetry());
        RetryConfig fooRetry = foo.getRetry();
        // Global override
        assertEquals(fooRetry.get(RetryConfig.DELAY, Long.class), Long.valueOf(10));
        // Method-level
        assertEquals(fooRetry.get(RetryConfig.MAX_RETRIES, Integer.class), Integer.valueOf(2));
        // Default value
        assertEquals(fooRetry.get(RetryConfig.DELAY_UNIT, ChronoUnit.class), ChronoUnit.MILLIS);

        FaultToleranceOperation bar = ops.get(FaultyService.class.getMethod("bar").toGenericString());
        assertNotNull(bar);
        assertTrue(bar.hasRetry());
        RetryConfig barRetry = bar.getRetry();
        // Global override
        assertEquals(barRetry.get(RetryConfig.DELAY, Long.class), Long.valueOf(10));
        // Class-level
        assertEquals(barRetry.get(RetryConfig.MAX_RETRIES, Integer.class), Integer.valueOf(1));
        // Default value
        assertEquals(fooRetry.get(RetryConfig.DELAY_UNIT, ChronoUnit.class), ChronoUnit.MILLIS);
    }
}
