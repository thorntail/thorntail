package org.jboss.unimbus.jms.impl.opentracing;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.jms.Message;
import javax.jms.MessageListener;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;
import org.jboss.unimbus.opentracing.TracingDecorator;
import org.jboss.unimbus.util.Annotations;

/**
 * Created by bob on 2/21/18.
 */
@RequiredClassPresent("org.eclipse.microprofile.opentracing.Traced")
@Dependent
@Decorator
@Traced
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@TracingDecorator({MessageListener.class})
public class TracedMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        if (Annotations.hasAnnotation(this.delegate, Traced.class)) {
            onMessageTraced(message);
        } else {
            onMessageNotTraced(message);
        }
    }

    protected void onMessageNotTraced(Message message) {
        this.delegate.onMessage(message);
    }

    protected void onMessageTraced(Message message) {
        ActiveSpan span = null;
        try {
            SpanContext parent = TraceUtils.extract(message);
            Tracer.SpanBuilder builder = TraceUtils.build("jms-receive", message);
            if (builder != null) {
                if (parent != null) {
                    builder.asChildOf(parent);
                }
                builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
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
    MessageListener delegate;

}
