package io.thorntail.jpa.impl.opentracing;

import io.opentracing.Scope;
import java.util.function.Consumer;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public interface TraceInfo {

    TraceMode traceMode();

    default TraceInfo withDecorator(Consumer<Scope> decorator) {
        return new DecoratedTraceInfo(this, decorator );
    }

    default Consumer<Scope> decorator() {
        return (in)->{};
    }

    default <R> R trace(String operationName, Traceable<R> code) {
        if (traceMode() == TraceMode.OFF) {
            return code.execute();
        }
        Tracer tracer = GlobalTracer.get();
        Scope parent = tracer.scopeManager().active();
        if (traceMode() == TraceMode.ACTIVE) {
            if (parent == null) {
                return code.execute();
            }
        }

        Tracer.SpanBuilder builder = tracer.buildSpan(operationName);
        if (parent != null) {
            builder.asChildOf(parent.span());
        }
        Scope span = builder.startActive(true);

        decorator().accept(span);

        try {
            return code.execute();
        } finally {
            span.close();
        }
    }
}
