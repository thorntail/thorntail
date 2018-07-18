package io.thorntail.testsuite.tracing;

import io.opentracing.Scope;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.InvocationContext;

import io.thorntail.tracing.SpanHandler;

@Priority(1)
@Dependent
public class AlphaHandler extends SpanHandler {

    static final AtomicInteger HANDLED_COUNTER = new AtomicInteger();

    static final AtomicInteger CREATED_COUNTER = new AtomicInteger();

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return ctx.getMethod().getName().equals("alpha") || ctx.getMethod().getName().equals("bravo");
    }

    @Override
    public Scope handle(InvocationContext ctx) {
        HANDLED_COUNTER.incrementAndGet();
        return null;
    }

    @PostConstruct
    void init() {
        CREATED_COUNTER.incrementAndGet();
    }

}
