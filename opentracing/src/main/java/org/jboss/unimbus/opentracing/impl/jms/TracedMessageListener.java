package org.jboss.unimbus.opentracing.impl.jms;

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
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.util.AnnotationUtils;

/**
 * Created by bob on 2/21/18.
 */
@Dependent
@Decorator
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TracedMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        if (AnnotationUtils.hasAnnotation(this.delegate, Traced.class)) {
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
    MessageListener delegate;

}
