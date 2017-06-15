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

package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension.HystrixExtension;

import com.netflix.hystrix.exception.HystrixRuntimeException;

/**
 * @author Antoine Sabot-Durand
 */
public class CommandInterceptorTest {

    @Rule
    public WeldInitiator weld = WeldInitiator
            .from(WeldInitiator.createWeld().addExtension(new HystrixExtension()).addPackages(true, DefaultCommand.class, getClass())).inject(this).build();

    @Inject
    MyMicroservice service;

    @Test
    public void shouldRunWithLongExecutionTime() {
        assertEquals(MyMicroservice.HELLO, service.sayHello());
    }

    @Test
    public void testTimeoutFallback() {
        MyFallbackHandler.reset();
        assertEquals(MyFallbackHandler.FALLBACK, service.sayHelloWithFallback());
        assertTrue(MyFallbackHandler.DISPOSED.get());
    }

    // TODO: should throw TimeoutException instead!
    @Test(expected = HystrixRuntimeException.class)
    public void testTimeoutNoFallback() {
        service.sayHelloTimeoutNoFallback();
        fail();
    }

    @Test
    public void testHelloAsync() throws InterruptedException, ExecutionException {
        Object result = service.sayHelloAsync();
        assertTrue(result instanceof Future);
        @SuppressWarnings("unchecked")
        Future<String> future = (Future<String>) result;
        assertEquals(MyMicroservice.HELLO, future.get());
    }

    @Test
    public void testHelloAsyncTimeoutFallback() throws InterruptedException, ExecutionException {
        Object result = service.sayHelloAsyncTimeoutFallback();
        assertTrue(result instanceof Future);
        @SuppressWarnings("unchecked")
        Future<String> future = (Future<String>) result;
        assertEquals(MyFallbackHandler.FALLBACK, future.get());
    }

}