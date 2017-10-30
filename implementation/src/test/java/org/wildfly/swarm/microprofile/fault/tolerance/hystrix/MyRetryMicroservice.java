package org.wildfly.swarm.microprofile.fault.tolerance.hystrix;

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
