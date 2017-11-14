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

package org.wildfly.swarm.microprofile.faulttolerance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;


/**
 * @author Antoine Sabot-Durand
 */
@ApplicationScoped
@CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 4, failureRatio=0.75, delay = 50000)
public class MyMicroservice {

    @Timeout(500)
    public String sayHello() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Timeout(200)
    @Fallback(MyFallbackHandler.class)
    public String sayHelloWithFallback() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Timeout(200)
    public String sayHelloTimeoutNoFallback() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return HELLO;
    }

    @Asynchronous
    public Future<String> sayHelloAsync() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(HELLO);
    }

    @Asynchronous
    @Timeout(200)
    @Fallback(FutureStringFallbackHandler.class)
    public Future<String> sayHelloAsyncTimeoutFallback() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(HELLO);
    }

    @CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 4, failureRatio=0.75, delay = 50000)
    public String sayHelloBreaker() {
        sayHelloBreakerCount ++;
        if(sayHelloBreakerCount < 5) {
            throw new RuntimeException("Connection failed");
        }
        return "sayHelloBreaker#"+sayHelloBreakerCount;
    }

    public String sayHelloBreakerClassLevel() {
        sayHelloBreakerCount3 ++;
        if(sayHelloBreakerCount3 < 5) {
            throw new RuntimeException("Connection failed");
        }
        return "sayHelloBreakerClassLevel#"+sayHelloBreakerCount3;
    }
    public int getSayHelloBreakerCount3() {
        return sayHelloBreakerCount3;
    }

    public int getSayHelloBreakerCount() {
        return sayHelloBreakerCount;
    }

    @CircuitBreaker(successThreshold = 1, requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
    public String sayHelloBreaker2() {
        sayHelloBreakerCount2 ++;
        // Only one execution succeeds
        if(sayHelloBreakerCount2 != 5) {
            throw new RuntimeException("Connection failed");
        }
        return "sayHelloBreaker#"+sayHelloBreakerCount2;
    }
    public int getSayHelloBreakerCount2() {
        return sayHelloBreakerCount2;
    }

    @CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 2, failureRatio = 1, delay = 50000)
    public String sayHelloBreakerOverride() {
        sayHelloBreakerCount4 ++;
        // Only one execution succeeds
        if(sayHelloBreakerCount4 != 5) {
            throw new RuntimeException("Connection failed");
        }
        return "sayHelloBreaker#"+sayHelloBreakerCount2;
    }
    public int getSayHelloBreakerCount4() {
        return sayHelloBreakerCount4;
    }

    @CircuitBreaker(successThreshold = 3, requestVolumeThreshold = 4, failureRatio=0.75, delay = 1000)
    public String sayHelloBreakerHighThreshold() {
        sayHelloBreakerCount5 ++;
        System.err.printf("sayHelloBreakerHighThreshold, %d\n", sayHelloBreakerCount5);
        // Only two execution succeeds
        if(sayHelloBreakerCount5 < 5 || sayHelloBreakerCount5 > 6) {
            throw new RuntimeException("Connection failed");
        }
        return "sayHelloBreaker#"+sayHelloBreakerCount5;
    }
    public int getSayHelloBreakerCount5() {
        return sayHelloBreakerCount5;
    }

    private int sayHelloBreakerCount;
    private int sayHelloBreakerCount2;
    private int sayHelloBreakerCount3;
    private int sayHelloBreakerCount4;
    private int sayHelloBreakerCount5;
    static final String HELLO = "Hello";
}
