package io.thorntail.opentracing.tck;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;

/**
 * Created by bob on 2/19/18.
 */
public class MockTracerResolver extends TracerResolver {

    @Override
    protected Tracer resolve() {
        return new MockTracer();
    }
}
