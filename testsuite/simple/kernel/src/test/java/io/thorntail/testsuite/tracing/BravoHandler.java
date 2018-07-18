package io.thorntail.testsuite.tracing;

import io.opentracing.Scope;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

import io.thorntail.tracing.SpanHandler;

@Priority(2)
@ApplicationScoped
public class BravoHandler extends SpanHandler {

    static final AtomicInteger HANDLED_COUNTER = new AtomicInteger();

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return ctx.getMethod().getName().equals("bravo");
    }

    @Override
    public Scope handle(InvocationContext ctx) {
        HANDLED_COUNTER.incrementAndGet();
        return null;
    }

}
