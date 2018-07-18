package io.thorntail.jpa.impl.opentracing;

import io.opentracing.Scope;
import java.util.function.Consumer;

import io.thorntail.TraceMode;

/**
 * Created by bob on 2/27/18.
 */
public class DecoratedTraceInfo implements TraceInfo {

    public DecoratedTraceInfo(TraceInfo delegate, Consumer<Scope> decorator) {
        this.delegate = delegate;
        this.decorator = decorator;
    }

    @Override
    public TraceMode traceMode() {
        return delegate.traceMode();
    }

    @Override
    public Consumer<Scope> decorator() {
        return delegate.decorator().andThen(this.decorator);
    }

    private final TraceInfo delegate;

    private final Consumer<Scope> decorator;
}
