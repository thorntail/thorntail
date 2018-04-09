package org.wildfly.swarm.microprofile.faulttolerance.deployment.async.retry;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

@ApplicationScoped
public class AsyncHelloService {

    static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Asynchronous
    @Retry(maxRetries = 2)
    public Future<String> retry(Result result) throws IOException {
        COUNTER.incrementAndGet();
        switch (result) {
            case FAILURE:
                throw new IOException("Simulated IO error");
            case COMPLETE_EXCEPTIONALLY:
                CompletableFuture<String> future = new CompletableFuture<>();
                future.completeExceptionally(new IOException("Simulated IO error"));
                return future;
            default:
                return completedFuture("Hello");
        }
    }

    @Asynchronous
    @Retry(maxRetries = 2)
    @Fallback(fallbackMethod = "fallback")
    public Future<String> retryWithFallback(Result result) throws IOException {
        return retry(result);
    }

    public Future<String> fallback(Result result) {
        return completedFuture("Fallback");
    }

    enum Result {

        SUCCESS, FAILURE, COMPLETE_EXCEPTIONALLY

    }

}
