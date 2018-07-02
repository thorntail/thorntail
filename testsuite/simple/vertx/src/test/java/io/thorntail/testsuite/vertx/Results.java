package io.thorntail.testsuite.vertx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Results {

    private final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(0));

    private final List<Object> messages = new CopyOnWriteArrayList<>();

    void reset(int expectedCount) {
        latch.set(new CountDownLatch(expectedCount));
        messages.clear();
    }

    void add(Object message) {
        messages.add(message);
        latch.get().countDown();
    }

    List<Object> getMessages() {
        return messages;
    }

    void await() throws InterruptedException {
        latch.get().await(5, TimeUnit.SECONDS);
    }
}
