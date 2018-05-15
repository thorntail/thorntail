package io.thorntail.jpa.impl.opentracing;

import java.util.function.Consumer;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public interface TraceInfo {

    TraceMode traceMode();

    default TraceInfo withDecorator(Consumer<ActiveSpan> decorator) {
        return new DecoratedTraceInfo(this, decorator );
    }

    default Consumer<ActiveSpan> decorator() {
        return (in)->{};
    }

    default <R> R trace(String operationName, Traceable<R> code) {
        if (traceMode() == TraceMode.OFF) {
            return code.execute();
        }
        Tracer tracer = GlobalTracer.get();
        ActiveSpan parent = tracer.activeSpan();
        if (traceMode() == TraceMode.ACTIVE) {
            if (parent == null) {
                return code.execute();
            }
        }

        Tracer.SpanBuilder builder = tracer.buildSpan(operationName);
        if (parent != null) {
            builder.asChildOf(parent);
        }
        ActiveSpan span = builder.startActive();

        decorator().accept(span);

        try {
            return code.execute();
        } finally {
            span.deactivate();
        }
    }
}
