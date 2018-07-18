package io.thorntail.jpa.impl.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import java.util.function.Consumer;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public interface TraceInfo {

    TraceMode traceMode();

    default TraceInfo withDecorator(Consumer<Span> decorator) {
        return new DecoratedTraceInfo(this, decorator );
    }

    default Consumer<Span> decorator() {
        return (in)->{};
    }

    default <R> R trace(String operationName, Traceable<R> code) {
        if (traceMode() == TraceMode.OFF) {
            return code.execute();
        }
        Tracer tracer = GlobalTracer.get();
        if (traceMode() == TraceMode.ACTIVE && tracer.activeSpan() == null) {
              return code.execute();
        }

        Scope scope = tracer.buildSpan(operationName)
            .startActive(true);

        decorator().accept(scope.span());

        try {
            return code.execute();
        } finally {
            scope.close();
        }
    }
}
