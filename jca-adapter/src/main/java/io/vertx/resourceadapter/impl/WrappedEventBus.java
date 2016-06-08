package io.vertx.resourceadapter.impl;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.WriteStream;
import io.vertx.resourceadapter.VertxEventBus;

import java.util.logging.Logger;

/**
 * @author Lin Gao <lgao@redhat.com>
 *
 */
@SuppressWarnings("unused")
public class WrappedEventBus implements VertxEventBus {

  /** The logger */
  private static Logger log = Logger.getLogger(WrappedEventBus.class.getName());

  private final EventBus delegate;

  public WrappedEventBus(EventBus bus) {
    super();
    if (bus == null) {
      throw new IllegalArgumentException("EventBus can't be null.");
    }
    this.delegate = bus;
  }

  public VertxEventBus send(String address, Object message) {
    this.delegate.send(address, message);
    return this;
  }

  @Override
  public VertxEventBus send(String address, Object message,
      DeliveryOptions options) {
    this.delegate.send(address, message, options);
    return this;
  }

  @Override
  public VertxEventBus publish(String address, Object message) {
    this.delegate.publish(address, message);
    return this;
  }

  @Override
  public VertxEventBus publish(String address, Object message,
      DeliveryOptions options) {
    this.delegate.publish(address, message, options);
    return this;
  }

  @Override
  public <T> WriteStream<T> sender(String address) {
    return this.delegate.sender(address);
  }

  @Override
  public <T> WriteStream<T> sender(String address, DeliveryOptions options) {
    return this.delegate.sender(address, options);
  }

  @Override
  public <T> WriteStream<T> publisher(String address) {
    return this.delegate.publisher(address);
  }

  @Override
  public <T> WriteStream<T> publisher(String address, DeliveryOptions options) {
    return this.delegate.publisher(address, options);
  }


}
