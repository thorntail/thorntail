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

/**
 * Created by bob on 2/21/18.
 */
@Dependent
@Decorator
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TracedMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
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
