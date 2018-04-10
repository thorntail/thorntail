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
package org.wildfly.swarm.microprofile.faulttolerance.deployment.fallbackmethod.nonpublic;


import static org.junit.Assert.assertEquals;

import java.util.Collections;

import javax.inject.Inject;

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
public class NonPublicFallbackMethodTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(NonPublicFallbackMethodTest.class)
                .addPackage(NonPublicFallbackMethodTest.class.getPackage());
    }

    @Inject
    FaultyService service;

    @Test
    public void testFallbackMethod() throws NoSuchMethodException, SecurityException {
        FaultyService.COUNTER.set(0);
        assertEquals(service.foo(), 1);
        assertEquals(FaultyService.COUNTER.get(), 3);
    }

    @Test
    public void testFallbackMethodParameterizedReturnType() throws NoSuchMethodException, SecurityException {
        FaultyService.COUNTER.set(0);
        assertEquals(service.fooParameterized(), Collections.emptyList());
        assertEquals(FaultyService.COUNTER.get(), 3);
    }
}
