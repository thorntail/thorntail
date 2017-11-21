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
package org.wildfly.swarm.microprofile.faulttolerance.fallbackmethod.nonpublic;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.faulttolerance.TestArchive;

/**
 *
 * @author Martin Kouba
 */
public class NonPublicFallbackMethodTest extends Arquillian {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase().addPackage(NonPublicFallbackMethodTest.class.getPackage());
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
