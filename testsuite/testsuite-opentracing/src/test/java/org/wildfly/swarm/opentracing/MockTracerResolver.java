package org.wildfly.swarm.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;

/**
 * @author Juraci Paixão Kröhling
 */
public class MockTracerResolver extends TracerResolver {
    @Override
    protected Tracer resolve() {
        return new MockTracer();
    }
}
