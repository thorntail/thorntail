package org.jboss.unimbus.opentracing.impl;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.opentracing.TracerProvider;

/**
 * Created by bob on 2/21/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.opentracing.mock.MockTracer")
@Priority(Integer.MIN_VALUE)
public class MockTracerProvider implements TracerProvider {

    @Override
    public Tracer get() {
        return new MockTracer(new ThreadLocalActiveSpanSource(), MockTracer.Propagator.TEXT_MAP);
    }

}
