package org.wildfly.swarm.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * @author Juraci Paixão Kröhling
 */
public class MockTracerResolver extends TracerResolver {
    static final MockTracer TRACER_INSTANCE = new MockTracer();

    @Override
    protected Tracer resolve() {
        return TRACER_INSTANCE;
    }
}
