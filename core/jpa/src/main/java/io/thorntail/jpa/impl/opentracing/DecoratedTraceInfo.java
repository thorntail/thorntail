package io.thorntail.jpa.impl.opentracing;

import java.util.function.Consumer;

import io.opentracing.ActiveSpan;
import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public class DecoratedTraceInfo implements TraceInfo {

    public DecoratedTraceInfo(TraceInfo delegate, Consumer<ActiveSpan> decorator) {
        this.delegate = delegate;
        this.decorator = decorator;
    }

    @Override
    public TraceMode traceMode() {
        return delegate.traceMode();
    }

    @Override
    public Consumer<ActiveSpan> decorator() {
        return delegate.decorator().andThen(this.decorator);
    }

    private final TraceInfo delegate;

    private final Consumer<ActiveSpan> decorator;
}
