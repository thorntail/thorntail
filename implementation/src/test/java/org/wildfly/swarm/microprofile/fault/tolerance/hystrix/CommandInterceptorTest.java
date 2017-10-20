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



import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.fault.tolerance.hystrix.extension.HystrixExtension;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Antoine Sabot-Durand
 */
public class CommandInterceptorTest extends Arquillian {


    @Deployment
    public static JavaArchive deploy() {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "CommandInterceptorTest.jar")
                .addPackages(true, "org.wildfly.swarm.microprofile.fault.tolerance.hystrix")
                .addAsServiceProvider(Extension.class,HystrixExtension.class)
                .addAsManifestResource(EmptyAsset.INSTANCE,"beans.xml");

        return testJar;
    }


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
    @Test(expectedExceptions = RuntimeException.class)
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