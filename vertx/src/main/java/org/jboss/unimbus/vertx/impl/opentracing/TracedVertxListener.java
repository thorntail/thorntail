package org.jboss.unimbus.vertx.impl.opentracing;

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
import io.opentracing.tag.Tags;
import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.inflow.VertxListener;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.opentracing.TracingDecorator;
import org.jboss.unimbus.util.Annotations;

/**
 * Created by bob on 2/22/18.
 */
@RequiredClassPresent("org.eclipse.microprofile.opentracing.Traced")
@Dependent
@Decorator
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@TracingDecorator({VertxListener.class})
@Traced
public class TracedVertxListener implements VertxListener {

    @Override
    public <T> void onMessage(Message<T> message) {
        if (Annotations.hasAnnotation(this.delegate, Traced.class)) {
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
            Tracer.SpanBuilder builder = TraceUtils.build("vertx-receive", message);
            if (builder != null) {
                if (parent != null) {
                    builder.asChildOf(parent);
                }
                span = builder.startActive();
                Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CONSUMER);
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
