package org.jboss.unimbus.tracing.impl.mock;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.tracing.TracerProvider;

/**
 * Created by bob on 2/21/18.
 */
@ApplicationScoped
@RequiredClassPresent("io.opentracing.mock.MockTracer")
@Priority(0)
public class MockTracerProvider implements TracerProvider {

    @Override
    public Tracer get() {
        return new MockTracer(new ThreadLocalActiveSpanSource(), MockTracer.Propagator.TEXT_MAP);
    }

}
