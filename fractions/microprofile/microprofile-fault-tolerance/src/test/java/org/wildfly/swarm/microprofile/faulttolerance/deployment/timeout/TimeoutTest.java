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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.timeout;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class TimeoutTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(TimeoutTest.class).addPackage(TimeoutTest.class.getPackage());
    }

    @Inject
    TimingOutService service;

    @Test
    public void testTimeout() throws InterruptedException {
        TimingOutService.INTERRUPTED.set(false);
        boolean timeoutExceptionThrown = false;
        try {
            service.someSlowMethod(1500);
            fail("No timeout");
        } catch (TimeoutException expected) {
            timeoutExceptionThrown = true;
        } catch (Exception e) {
            fail("Unexpected: " + e.getMessage());
        }
        assertTrue(TimingOutService.INTERRUPTED.get());
        assertTrue(timeoutExceptionThrown);
    }

}