package org.wildfly.swarm.microprofile.faulttolerance.deployment.async.circuitbreaker;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

@ApplicationScoped
public class AsyncHelloService {

    static final AtomicInteger COUNTER = new AtomicInteger(0);

    static final int THRESHOLD = 10;

    static final int DELAY = 500;

    static final String OK = "Hello";

    @Asynchronous
    @CircuitBreaker(requestVolumeThreshold = THRESHOLD, failureRatio = 0.5, delay = DELAY, successThreshold = 1)
    public Future<String> hello(Result result) throws IOException {
        COUNTER.incrementAndGet();
        switch (result) {
            case FAILURE:
                throw new IOException("Simulated IO error");
            case COMPLETE_EXCEPTIONALLY:
                CompletableFuture<String> future = new CompletableFuture<>();
                future.completeExceptionally(new IOException("Simulated IO error"));
                return future;
            default:
                return completedFuture(OK);
        }
    }

    enum Result {

        SUCCESS, FAILURE, COMPLETE_EXCEPTIONALLY

    }

}
