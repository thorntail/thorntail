package org.jboss.unimbus.opentracing.impl.vertx;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.inflow.VertxListener;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.cdi.AnnotationUtils;

/**
 * Created by bob on 2/22/18.
 */
@Dependent
@Decorator
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TracedVertxListener implements VertxListener {

    @Override
    public <T> void onMessage(Message<T> message) {
        if (AnnotationUtils.hasAnnotation(this.delegate, Traced.class)) {
            onMessageTraced(message);
        } else {
            onMessageNotTraced(message);
        }
    }

    private <T> void onMessageNotTraced(Message<T> message) {
        this.delegate.onMessage(message);
    }

    private <T> void onMessageTraced(Message<T> message) {

        ActiveSpan span = null;
        try {
            SpanContext parent = TraceUtils.extract(message);
            Tracer.SpanBuilder builder = TraceUtils.build("receive", message);
            if (builder != null) {
                if (parent != null) {
                    builder.asChildOf(parent);
                }
                span = builder.startActive();
            }
            this.delegate.onMessage(message);
        } finally {
            if (span != null) {
                span.deactivate();
            }
        }
    }

    @Inject
    @Delegate
    @Any
    VertxListener delegate;
}
