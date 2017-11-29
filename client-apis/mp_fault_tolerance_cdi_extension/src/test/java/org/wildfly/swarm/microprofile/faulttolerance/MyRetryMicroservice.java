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

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class MyRetryMicroservice {

    @Retry(maxRetries = 1)
    @Fallback(StringFallbackHandler.class)
    public String serviceA() {
        counterForInvokingServiceA++;
        throw new RuntimeException("Connection failed");
    }
    @Retry(maxRetries = 2)
    @Fallback(StringFallbackHandler.class)
    public String serviceB() {
        counterForInvokingServiceB++;
        throw new RuntimeException("Connection failed");
    }

    @CircuitBreaker(successThreshold = 2, requestVolumeThreshold = 4, failureRatio = 0.75, delay = 50000)
    @Retry(retryOn = {RuntimeException.class}, maxRetries = 7)
    public String sayHelloRetry() {
        sayHelloRetry ++;
        throw new RuntimeException("Connection failed");
    }
    public int getSayHelloRetry() {
        return sayHelloRetry;
    }

    @Timeout(500)
    @Retry(maxRetries = 1)
    public String serviceA(long timeToSleep) {
        try {
            counterForInvokingServiceA++;
            Thread.sleep(timeToSleep);
            throw new RuntimeException("Timeout did not interrupt");
        }
        catch (InterruptedException e) {
            //expected
        }
        return null;
    }
    public int getCounterForInvokingServiceA() {
        return counterForInvokingServiceA;
    }
    public int getCounterForInvokingServiceB() {
        return counterForInvokingServiceB;
    }

    public int getCounterForInvokingServiceC() {
        return counterForInvokingServiceC;
    }

    public int getCounterForInvokingServiceD() {
        return counterForInvokingServiceD;
    }

    public int getCounterForInvokingServiceE() {
        return counterForInvokingServiceE;
    }

    private int sayHelloRetry;
    private int counterForInvokingServiceA = 0;
    private int counterForInvokingServiceB = 0;
    private int counterForInvokingServiceC = 0;
    private int counterForInvokingServiceD = 0;
    private int counterForInvokingServiceE = 0;


}
