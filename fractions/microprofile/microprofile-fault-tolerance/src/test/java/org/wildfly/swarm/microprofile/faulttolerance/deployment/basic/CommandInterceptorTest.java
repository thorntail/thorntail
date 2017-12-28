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

package org.wildfly.swarm.microprofile.faulttolerance.deployment.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.faulttolerance.deployment.TestArchive;

/**
 * @author Antoine Sabot-Durand
 */
@RunWith(Arquillian.class)
public class CommandInterceptorTest {

    @Deployment
    public static JavaArchive deploy() {
        return TestArchive.createBase("CommandInterceptorTest.jar").addPackages(false, CommandInterceptorTest.class.getPackage());
    }

    @Inject
    MyMicroservice service;

    @Inject
    MyRetryMicroservice serviceRetry;

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
    @Test(expected = RuntimeException.class)
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

    @Test
    public void testSayHelloBreaker() {
        for (int n = 0; n < 7; n++) {
            try {
                String result = service.sayHelloBreaker();
                System.out.printf("%d: Result: %s\n", n, result);
                System.out.flush();
            } catch (Exception e) {
                System.out.printf("%d: Saw exception: %s\n", n, e);
                System.out.flush();
            }
        }
        int count = service.getSayHelloBreakerCount();
        assertEquals("The number of executions should be 4", count, 4);

    }

    @Test
    public void testSayHelloBreakerClassLevel() {
        for (int n = 0; n < 7; n++) {
            try {
                String result = service.sayHelloBreakerClassLevel();
                System.out.printf("%d: Result: %s\n", n, result);
                System.out.flush();
            } catch (Exception e) {
                System.out.printf("%d: Saw exception: %s\n", n, e);
                System.out.flush();
            }
        }
        int count = service.getSayHelloBreakerCount3();
        assertEquals("The number of executions should be 4", count, 4);

    }

    @Test
    public void testSayHelloBreaker2() {
        for (int i = 1; i < 12; i++) {

            try {
                String result = service.sayHelloBreaker2();
                System.out.printf("%d, %s\n", i, result);

                if (i < 5 || (i > 6 && i < 12)) {
                    fail("serviceA should throw an Exception in testCircuitDefaultSuccessThreshold on iteration " + i);
                }
            } catch (CircuitBreakerOpenException cboe) {
                // Expected on execution 5 and iteration 10
                System.out.printf("%d, CircuitBreakerOpenException\n", i);

                if (i < 5) {
                    fail("serviceA should throw a RuntimeException in testCircuitDefaultSuccessThreshold on iteration " + i);
                } else if (i == 5) {
                    // Pause to allow the circuit breaker to half-open
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (RuntimeException ex) {
                // Expected
                System.out.printf("%d, RuntimeException\n", i);
            } catch (Exception ex) {
                // Not Expected
                fail("serviceA should throw a RuntimeException or CircuitBreakerOpenException in testCircuitDefaultSuccessThreshold " + "on iteration " + i);
            }
        }
        int count = service.getSayHelloBreakerCount2();
        assertEquals("The number of serviceA executions should be 9", count, 9);
    }

    @Test
    public void testClassLevelCircuitOverride() {
        for (int i = 0; i < 7; i++) {
            try {
                service.sayHelloBreakerOverride();

                if (i < 2) {
                    fail("sayHelloBreakerOverride should throw an Exception in testClassLevelCircuitOverride on iteration " + i);
                }
            } catch (CircuitBreakerOpenException cboe) {
                // Expected on iteration 4
                if (i < 2) {
                    fail("sayHelloBreakerOverride should throw a RuntimeException in testClassLevelCircuitOverride on iteration " + i);
                }
            } catch (RuntimeException ex) {
                // Expected
            } catch (Exception ex) {
                // Not Expected
                fail("sayHelloBreakerOverride should throw a RuntimeException or CircuitBreakerOpenException in testClassLevelCircuitOverride "
                        + "on iteration " + i);
            }
        }

        int count = service.getSayHelloBreakerCount4();

        assertEquals("The number of executions should be 2", count, 2);
    }

    // @Test(enabled = true, description = "Still trying to figure out @CircuitBreaker(successThreshold=...)")
    @Test
    public void testCircuitHighSuccessThreshold() {
        for (int i = 1; i < 10; i++) {

            try {
                service.sayHelloBreakerHighThreshold();

                if (i < 5 || i > 7) {
                    fail("serviceA should throw an Exception in testCircuitHighSuccessThreshold on iteration " + i);
                }
            } catch (CircuitBreakerOpenException cboe) {
                // Expected on iteration 4 and iteration 10
                if (i < 5) {
                    fail("serviceA should throw a RuntimeException in testCircuitHighSuccessThreshold on iteration " + i);
                } else if (i == 5) {
                    // Pause to allow the circuit breaker to half-open
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (RuntimeException ex) {
                // Expected
            } catch (Exception ex) {
                // Not Expected
                fail("serviceA should throw a RuntimeException or CircuitBreakerOpenException in testCircuitHighSuccessThreshold " + "on iteration " + i);
            }
        }
        int count = service.getSayHelloBreakerCount5();

        assertEquals("The number of serviceA executions should be 7", count, 7);
    }

    /**
     * A test to exercise Circuit Breaker thresholds with sufficient retries to open the Circuit and result in a CircuitBreakerOpenException.
     */
    @Test
    public void testCircuitOpenWithMoreRetries() {
        int invokeCounter = 0;
        try {
            serviceRetry.sayHelloRetry();
            invokeCounter = serviceRetry.getSayHelloRetry();
            if (invokeCounter < 4) {
                fail("serviceA should retry in testCircuitOpenWithMoreRetries on iteration " + invokeCounter);
            }
        } catch (CircuitBreakerOpenException cboe) {
            // Expected on iteration 4
            invokeCounter = serviceRetry.getSayHelloRetry();
            if (invokeCounter < 4) {
                fail("serviceA should retry in testCircuitOpenWithMoreRetries on iteration " + invokeCounter);
            }
        } catch (Exception ex) {
            // Not Expected
            invokeCounter = serviceRetry.getSayHelloRetry();
            fail("serviceA should retry or throw a CircuitBreakerOpenException in testCircuitOpenWithMoreRetries on iteration " + invokeCounter);
        }

        invokeCounter = serviceRetry.getSayHelloRetry();
        assertEquals("The number of executions should be 4", invokeCounter, 4);
    }

    @Test
    public void testRetryTimeout() {
        try {
            String result = serviceRetry.serviceA(1000);
        } catch (TimeoutException ex) {
            // Expected
        } catch (RuntimeException ex) {
            // Not Expected
            Assert.fail("serviceA should not throw a RuntimeException in testRetryTimeout");
        }

        assertEquals("The execution count should be 2 (1 retry + 1)", serviceRetry.getCounterForInvokingServiceA(), 2);
    }

    @Test
    public void testFallbackSuccess() {

        try {
            String result = serviceRetry.serviceA();
            Assert.assertTrue("The message should be \"fallback for serviceA\"", result.contains("serviceA"));
        } catch (RuntimeException ex) {
            Assert.fail("serviceA should not throw a RuntimeException in testFallbackSuccess");
        }
        assertEquals("The execution count should be 2 (1 retry + 1)", serviceRetry.getCounterForInvokingServiceA(), 2);
        try {
            String result = serviceRetry.serviceB();
            Assert.assertTrue("The message should be \"fallback for serviceB\"", result.contains("serviceB"));
        } catch (RuntimeException ex) {
            Assert.fail("serviceB should not throw a RuntimeException in testFallbackSuccess");
        }
        assertEquals("The execution count should be 3 (2 retries + 1)", serviceRetry.getCounterForInvokingServiceB(), 3);
    }
}