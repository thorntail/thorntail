package org.jboss.unimbus.jms.impl.opentracing;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

/**
 * Created by bob on 2/21/18.
 */
class TraceUtils {

    static final String JMS_CORRELATION_ID_TAG = "jms.correlation.id";

    static final String JMS_MESSAGE_ID_TAG = "jms.message.id";

    static String nameOf(Destination destination) {
        try {
            if (destination instanceof Topic) {
                return ((Topic) destination).getTopicName();
            }
            if (destination instanceof Queue) {
                return ((Queue) destination).getQueueName();
            }
        } catch (JMSException e) {
            // ignore
        }

        return destination.toString();
    }

    static SpanContext extract(Message message) {
        Tracer tracer = GlobalTracer.get();
        JMSMessageAdapter carrier = new JMSMessageAdapter(message);
        return tracer.extract(Format.Builtin.HTTP_HEADERS, carrier);
    }

    static void inject(Message message) {
        Tracer tracer = GlobalTracer.get();
        JMSMessageAdapter carrier = new JMSMessageAdapter(message);
        ActiveSpan span = tracer.activeSpan();
        if (span != null) {
            SpanContext context = tracer.activeSpan().context();
            tracer.inject(context, Format.Builtin.HTTP_HEADERS, carrier);
        }
    }

    static Tracer.SpanBuilder build(String operationName, Message message) {
        return build(operationName, message, null);
    }

    static Tracer.SpanBuilder build(String operationName, Message message, Destination destination) {
        Tracer tracer = GlobalTracer.get();
        Tracer.SpanBuilder builder = tracer.buildSpan(operationName);

        if (destination == null) {
            try {
                destination = message.getJMSDestination();
            } catch (JMSException e) {
                // ignore;
            }
        }
        if (destination != null) {
            builder.withTag(Tags.MESSAGE_BUS_DESTINATION.getKey(), nameOf(destination));
        }
        try {
            String correlationId = message.getJMSCorrelationID();
            if (correlationId != null) {
                builder.withTag(JMS_CORRELATION_ID_TAG, correlationId);
            }
        } catch (JMSException e) {
            // ignore;
        }
        try {
            String messageId = message.getJMSMessageID();
            if (messageId != null) {
                builder.withTag(JMS_MESSAGE_ID_TAG, messageId);
            }
        } catch (JMSException e) {
            // ignore
        }

        return builder;
    }
}
