package io.vertx.resourceadapter.inflow.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.impl.VertxHolder;
import io.vertx.resourceadapter.impl.VertxPlatformConfiguration;
import io.vertx.resourceadapter.impl.VertxPlatformFactory;
import io.vertx.resourceadapter.impl.VertxResourceAdapter;
import io.vertx.resourceadapter.inflow.VertxListener;

/**
 * VertxActivation
 */
public class VertxActivation<T> implements VertxPlatformFactory.VertxListener, VertxHolder {

    public VertxActivation(VertxResourceAdapter ra, MessageEndpointFactory endpointFactory, VertxActivationSpec spec)
            throws ResourceException {
        this.ra = ra;
        this.endpointFactory = endpointFactory;
        this.spec = spec;
        this.config = spec.getVertxPlatformConfig();
    }

    /**
     * Get activation spec class
     *
     * @return Activation spec
     */
    public VertxActivationSpec getActivationSpec() {
        return spec;
    }

    /**
     * Get message endpoint factory
     *
     * @return Message endpoint factory
     */
    public MessageEndpointFactory getMessageEndpointFactory() {
        return endpointFactory;
    }

    /**
     * Start the activation
     *
     * @throws ResourceException Thrown if an error occurs
     */
    public void start() throws ResourceException {

        if (!deliveryActive.get()) {
            VertxPlatformFactory.instance().getOrCreateVertx(config, this);
            VertxPlatformFactory.instance().addVertxHolder(this);
        }
        setup();
    }

    @Override
    public void whenReady(Vertx vertx) {
        if (deliveryActive.get()) {
            log.log(Level.WARNING, "Vertx has been started.");
            return;
        }
        this.vertx = vertx;
        setup();
        deliveryActive.set(true);
        VertxPlatformFactory.instance().addVertxHolder(this);
    }

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    /**
     * Stop the activation
     */
    public void stop() {
        tearDown();
        deliveryActive.set(false);
    }

    private void setup() {
        String address = this.spec.getAddress();
        try {
            final MessageEndpoint endPoint = endpointFactory.createEndpoint(null);
            this.messageHandler = message -> handleMessage(endPoint, message);

            if (this.vertx == null) {
                throw new ResourceException("Vertx platform did not start yet.");
            }
            vertx.eventBus().consumer(address).handler(messageHandler);
            log.log(Level.INFO,
                    "Endpoint created, register Vertx handler on address: " + address);
        } catch (Exception e) {
            throw new RuntimeException("Can't create the endpoint.", e);
        }
    }

    private void handleMessage(MessageEndpoint endPoint, Message<?> message) {
        try {
            ra.getWorkManager().scheduleWork(new HandleMessage(endPoint, message));
        } catch (WorkException e) {
            throw new RuntimeException("Can't handle message.", e);
        }
    }

    private void tearDown() {

        VertxPlatformFactory.instance().removeVertxHolder(this);
        VertxPlatformFactory.instance().stopPlatformManager(this.config);
    }

    private static final Logger log = Logger.getLogger(VertxActivation.class.getName());

    static {
        try {
            VertxListener.class.getMethod("onMessage", new Class[]{Message.class});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final VertxResourceAdapter ra;

    private final VertxActivationSpec spec;

    private final MessageEndpointFactory endpointFactory;

    private final VertxPlatformConfiguration config;

    private final AtomicBoolean deliveryActive = new AtomicBoolean(false);

    private Handler<Message<Object>> messageHandler;

    private Vertx vertx;

    private class HandleMessage implements Work {

        private HandleMessage(MessageEndpoint endPoint, Message<?> message) {
            this.endPoint = endPoint;
            this.message = message;
        }

        @Override
        public void run() {
            ((VertxListener) endPoint).onMessage(message);
        }

        @Override
        public void release() {
        }

        private final MessageEndpoint endPoint;

        private final Message<?> message;

    }

}
