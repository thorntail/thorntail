package io.thorntail.tracing.impl.mock;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.tracing.TracerProvider;

/**
 * Created by bob on 2/21/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.opentracing.mock.MockTracer")
@Priority(0)
public class MockTracerProvider implements TracerProvider {

    @Override
    public Tracer get() {
        return new MockTracer();
    }

}
