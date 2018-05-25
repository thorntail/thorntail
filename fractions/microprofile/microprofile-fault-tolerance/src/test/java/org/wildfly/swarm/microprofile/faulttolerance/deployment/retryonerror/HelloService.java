package org.wildfly.swarm.microprofile.faulttolerance.deployment.retryonerror;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Retry;

@ApplicationScoped
public class HelloService {

    static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Retry(maxRetries = 2, retryOn = Error.class)
    public String retry() {
        if (COUNTER.incrementAndGet() == 3) {
            return "ok";
        }
        throw new AssertionError();
    }

}
