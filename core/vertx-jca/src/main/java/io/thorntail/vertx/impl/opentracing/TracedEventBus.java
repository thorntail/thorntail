package io.thorntail.vertx.impl.opentracing;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.streams.WriteStream;
import io.vertx.resourceadapter.VertxEventBus;

/**
 * Created by bob on 3/1/18.
 */
public class TracedEventBus implements VertxEventBus {

    public TracedEventBus(VertxEventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public VertxEventBus send(String address, Object o) {
        send(address, o, new DeliveryOptions());
        return this;
    }

    @Override
    public VertxEventBus send(String address, Object o, DeliveryOptions deliveryOptions) {
        Tracer tracer = GlobalTracer.get();
        try (Scope scope = tracer.buildSpan("vertx-send").startActive(true)) {
            Tags.MESSAGE_BUS_DESTINATION.set(scope.span(), address);
            Tags.SPAN_KIND.set(scope.span(), Tags.SPAN_KIND_PRODUCER);
            TraceUtils.inject(deliveryOptions);
            delegate.send(address, o, deliveryOptions);
        }
        return this;
    }

    @Override
    public VertxEventBus publish(String address, Object o) {
        publish(address, o, new DeliveryOptions());
        return this;
    }

    @Override
    public VertxEventBus publish(String address, Object o, DeliveryOptions deliveryOptions) {
        Tracer tracer = GlobalTracer.get();
        Scope span = tracer.buildSpan("vertx-send").startActive(true);
        try {
            Tags.MESSAGE_BUS_DESTINATION.set(span.span(), address);
            Tags.SPAN_KIND.set(span.span(), Tags.SPAN_KIND_PRODUCER);
            TraceUtils.inject(deliveryOptions);
            delegate.publish(address, o, deliveryOptions);
            return this;
        } finally {
            if ( span != null ) {
                span.close();
            }
        }
    }

    @Override
    public <T> WriteStream<T> sender(String address) {
        return sender(address, new DeliveryOptions());
    }

    @Override
    public <T> WriteStream<T> sender(String address, DeliveryOptions deliveryOptions) {
        return new TracedWriteStream<>(delegate.sender(address, deliveryOptions));
    }

    @Override
    public <T> WriteStream<T> publisher(String address) {
        return publisher(address, new DeliveryOptions());
    }

    @Override
    public <T> WriteStream<T> publisher(String address, DeliveryOptions deliveryOptions) {
        return new TracedWriteStream<>(delegate.publisher(address, deliveryOptions));
    }

    private final VertxEventBus delegate;
}
