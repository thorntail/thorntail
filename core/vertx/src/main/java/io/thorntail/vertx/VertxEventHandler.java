package io.thorntail.vertx;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.event.Event;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

/**
 * An instance of this handler is registered per each address found by {@link org.jboss.weld.vertx.VertxExtension}}.
 *
 * @author Martin Kouba
 */
class VertxEventHandler implements Handler<Message<Object>> {

    private final Vertx vertx;

    private final Event<VertxMessage> event;

    private final boolean isBlocking;

    static VertxEventHandler from(Vertx vertx, Event<Object> event, String address, boolean isBlocking) {
        return new VertxEventHandler(vertx, event.select(VertxMessage.class, VertxConsume.Literal.of(address)), isBlocking);
    }

    private VertxEventHandler(Vertx vertx, Event<VertxMessage> event, boolean isBlocking) {
        this.vertx = vertx;
        this.event = event;
        this.isBlocking = isBlocking;
    }

    @Override
    public void handle(Message<Object> message) {
        VertxMessageImpl vertxMessage = new VertxMessageImpl(message);
        if (isBlocking) {
            // Notification is potentially a blocking code
            // The execution of the blocking code is not ordered - see Vertx.executeBlocking(Handler<Future<T>>, boolean, Handler<AsyncResult<T>>) javadoc
            vertx.<Object> executeBlocking(future -> {
                try {
                    // Synchronously notify all the observer methods for a specific address
                    event.fire(vertxMessage);
                    future.complete();
                } catch (Exception e) {
                    future.fail(e);
                }
            }, false, result -> {
                if (result.failed() && message.isSend()) {
                    message.fail(VertxMessage.OBSERVER_FAILURE_CODE, result.cause().getMessage());
                    VertxLogger.LOG.observerNotificationFailure(result.cause());
                }
            });
        } else {
            // Non-blocking observers
            try {
                event.fire(vertxMessage);
            } catch (Exception e) {
                if (message.isSend()) {
                    message.fail(VertxMessage.OBSERVER_FAILURE_CODE, e.getMessage());
                }
                VertxLogger.LOG.observerNotificationFailure(e);
            }
        }
    }

    class VertxMessageImpl implements VertxMessage {

        private final Message<Object> delegate;

        private final AtomicBoolean isReplied;

        VertxMessageImpl(Message<Object> message) {
            this.delegate = message;
            this.isReplied = new AtomicBoolean(false);
        }

        @Override
        public String address() {
            return delegate.address();
        }

        @Override
        public MultiMap headers() {
            return delegate.headers();
        }

        @Override
        public Object body() {
            return delegate.body();
        }

        @Override
        public String replyAddress() {
            return delegate.replyAddress();
        }

        @Override
        public void reply(Object message) {
            if (setReplied(message)) {
                delegate.reply(message);
            }
        }

        @Override
        public <R> void reply(Object message, Handler<AsyncResult<Message<R>>> replyHandler) {
            if (setReplied(message)) {
                delegate.reply(message, replyHandler);
            }
        }

        @Override
        public void reply(Object message, DeliveryOptions options) {
            if (setReplied(message)) {
                delegate.reply(message, options);
            }
        }

        @Override
        public <R> void reply(Object message, DeliveryOptions options, Handler<AsyncResult<Message<R>>> replyHandler) {
            if (setReplied(message)) {
                delegate.reply(message, options, replyHandler);
            }
        }

        @Override
        public boolean isReplied() {
            return isReplied.get();
        }

        public boolean isSend() {
            return delegate.isSend();
        }

        @Override
        public void fail(int code, String message) {
            if (setReplied(message)) {
                this.delegate.fail(code, message);
            }
        }

        private boolean setReplied(Object message) {
            if (delegate.replyAddress() == null) {
                VertxLogger.LOG.noReplyHandlerSet(address(), message);
                return false;
            }
            if (isReplied.compareAndSet(false, true)) {
                return true;
            }
            VertxLogger.LOG.replyAlreadySent(replyAddress(), message);
            return false;
        }

    }

}