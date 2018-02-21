package org.jboss.unimbus.jca.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Decorator;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.SubclassDecoratorApplyingInstantiator;

/**
 * Created by bob on 2/8/18.
 */
public class EndpointFactory implements MessageEndpointFactory {

    public EndpointFactory(String containerId, Class<?> instanceType, Class<?> listenerInterface) {
        this.containerId = containerId;
        this.instanceType = instanceType;
        this.listenerInterface = listenerInterface;
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        return create(xaResource);
    }

    @Override
    public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) throws UnavailableException {
        return create(xaResource);
    }

    @Override
    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        return false;
    }

    @Override
    public String getActivationName() {
        return null;
    }

    @Override
    public Class<?> getEndpointClass() {
        return this.listenerInterface;
    }

    MessageEndpoint create(XAResource xaResource) {
        BeanManager manager = CDI.current().getBeanManager();

        AnnotatedType type = manager.createAnnotatedType(this.instanceType);
        BeanInjectionTarget injectionTarget = (BeanInjectionTarget) manager.getInjectionTargetFactory(type).createInjectionTarget(null);
        List<Decorator<?>> decorators = manager.resolveDecorators(Collections.singleton(this.listenerInterface), Any.Literal.INSTANCE);

        EndpointBean bean = new EndpointBean(this.instanceType, this.listenerInterface, injectionTarget);

        if ( ! decorators.isEmpty() ) {
            Instantiator instantiator = injectionTarget.getInstantiator();
            injectionTarget.setInstantiator(new SubclassDecoratorApplyingInstantiator(this.containerId, instantiator, bean, decorators));
        }

        CreationalContext<Object> context = manager.createCreationalContext(null);

        Object instance = bean.create(context);

        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == MessageEndpoint.class) {
                if (method.getName().equals("release")) {
                    //injectionTarget.preDestroy(instance);
                    //injectionTarget.dispose(instance);
                    bean.destroy(instance, context);
                } else if ( method.getName().equals("beforeDelivery" ) ) {

                } else if ( method.getName().equals("afterDelivery" ) ) {

                }
            } else {
                return method.invoke(instance, args);
            }
            return null;
        };
        MessageEndpoint endpoint = (MessageEndpoint) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                                                            new Class[]{MessageEndpoint.class, this.listenerInterface},
                                                                            handler);

        return endpoint;
    }

    private final Class<?> listenerInterface;

    private final Class<?> instanceType;

    private final String containerId;
}
