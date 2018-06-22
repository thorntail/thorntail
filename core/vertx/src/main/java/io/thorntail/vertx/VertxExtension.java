package io.thorntail.vertx;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.inject.Singleton;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.util.reflection.Reflections;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * The central point of integration.
 *
 * <ul>
 * <li>Finds all CDI observer methods that should be notified when a message is sent via {@link EventBus}.</li>
 * <li>Finds all {@link Event} injection points that should be propagated to {@link EventBus}.</li>
 * <li>Finds and automatically deploys all verticles that are discovered as beans.</li>
 * <li>Registers a bean for {@link Vertx} instance.</li>
 * </ul>
 *
 * @author Martin Kouba
 * @see VertxMessage
 * @see VertxConsume
 * @see VertxPublish
 * @see VertxSend
 */
public class VertxExtension implements Extension {

    public static final long DEFAULT_CONSUMER_REGISTRATION_TIMEOUT = 10000l;

    private final Map<String, Boolean> consumerAddressToBlocking;

    private final List<InjectionPoint> eventInjectionPoints;

    private final AtomicReference<Vertx> vertx;

    public VertxExtension() {
        this.consumerAddressToBlocking = new HashMap<>();
        this.eventInjectionPoints = new ArrayList<>();
        this.vertx = new AtomicReference<>(null);
    }

    void processVertxEventObserver(@Observes ProcessObserverMethod<VertxMessage, ?> event) {
        VertxConsume consume = getVertxConsume(event.getObserverMethod().getObservedQualifiers());
        if (consume == null) {
            VertxLogger.LOG.vertxMessageObserverWithoutConsumeFound(event.getObserverMethod());
            return;
        }
        VertxLogger.LOG.vertxMessageObserverFound(event.getObserverMethod());
        consumerAddressToBlocking.compute(consume.value(), (k, v) -> v == null ? consume.blocking() : v || consume.blocking());
    }

    void processEventInjectionPoint(@Observes ProcessInjectionPoint<?, Event<?>> event) {
        if (getVertxPublish(event.getInjectionPoint().getQualifiers()) != null || getVertxSend(event.getInjectionPoint().getQualifiers()) != null) {
            eventInjectionPoints.add(event.getInjectionPoint());
        }
    }

    void registerBeansAfterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        // Add Vertx bean
        event.addBean().types(getBeanTypes(vertx.getClass(), Vertx.class)).addQualifiers(Any.Literal.INSTANCE, Default.Literal.INSTANCE).scope(Singleton.class)
                .createWith(c -> vertx.get());

        for (InjectionPoint injectionPoint : eventInjectionPoints) {
            VertxPublish publish = getVertxPublish(injectionPoint.getQualifiers());
            if (publish != null) {
                event.addObserverMethod().addQualifier(publish).observedType(getFacadeType(injectionPoint)).notifyWith(ctx -> {
                    VertxLogger.LOG.addPublishObserver(injectionPoint);
                    this.vertx.get().eventBus().publish(publish.value(), ctx.getEvent());
                });
            } else {
                VertxSend send = getVertxSend(injectionPoint.getQualifiers());
                if (send != null) {
                    event.addObserverMethod().addQualifier(send).observedType(getFacadeType(injectionPoint)).notifyWith(ctx -> {
                        VertxLogger.LOG.addSendObserver(injectionPoint);
                        this.vertx.get().eventBus().send(send.value(), ctx.getEvent());
                    });
                }
            }
        }
    }

    void registerComponents(Vertx vertx, Event<Object> event, BeanManager beanManager) {

        this.vertx.set(vertx);

        CountDownLatch latch = new CountDownLatch(consumerAddressToBlocking.size());
        for (Entry<String, Boolean> entry : consumerAddressToBlocking.entrySet()) {
            MessageConsumer<?> consumer = vertx.eventBus().consumer(entry.getKey(), VertxEventHandler.from(vertx, event, entry.getKey(), entry.getValue()));
            consumer.completionHandler(ar -> {
                if (ar.succeeded()) {
                    VertxLogger.LOG.registerConsumerOk(entry.getKey());
                    latch.countDown();
                } else {
                    VertxLogger.LOG.registerConsumerError(entry.getKey(), ar.cause());
                }
            });
        }
        long timeout = DEFAULT_CONSUMER_REGISTRATION_TIMEOUT;
        try {
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException(String.format("Vertx message consumers not registered within %s ms [registered: %s, total: %s]", timeout,
                        latch.getCount(), consumerAddressToBlocking.size()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private Set<Type> getBeanTypes(Class<?> implClazz, Type... types) {
        Set<Type> beanTypes = new HashSet<>();
        Collections.addAll(beanTypes, types);
        beanTypes.add(implClazz);
        // Add all the interfaces (and extended interfaces) implemented directly by the impl class
        beanTypes.addAll(Reflections.getInterfaceClosure(implClazz));
        return beanTypes;
    }

    private VertxConsume getVertxConsume(Set<Annotation> qualifiers) {
        Annotation qualifier = getQualifier(qualifiers, VertxConsume.class);
        return qualifier != null ? ((VertxConsume) qualifier) : null;
    }

    private VertxPublish getVertxPublish(Set<Annotation> qualifiers) {
        Annotation qualifier = getQualifier(qualifiers, VertxPublish.class);
        return qualifier != null ? ((VertxPublish) qualifier) : null;
    }

    private VertxSend getVertxSend(Set<Annotation> qualifiers) {
        Annotation qualifier = getQualifier(qualifiers, VertxSend.class);
        return qualifier != null ? ((VertxSend) qualifier) : null;
    }

    private Annotation getQualifier(Set<Annotation> qualifiers, Class<? extends Annotation> annotationType) {
        for (Annotation qualifier : qualifiers) {
            if (qualifier.annotationType().equals(annotationType)) {
                return qualifier;
            }
        }
        return null;
    }

    private Type getFacadeType(InjectionPoint injectionPoint) {
        Type genericType = injectionPoint.getType();
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException(injectionPoint.getType() + " not a ParameterizedType");
        }
    }

}
