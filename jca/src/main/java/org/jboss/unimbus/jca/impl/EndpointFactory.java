package org.jboss.unimbus.jca.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Unmanaged;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * Created by bob on 2/8/18.
 */
public class EndpointFactory implements MessageEndpointFactory {

    public EndpointFactory(Class<?> instanceType, Class<?> listenerInterface) {
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
        Unmanaged<?> unmanaged = new Unmanaged<>(this.instanceType);
        Unmanaged.UnmanagedInstance<?> unmanagedInstance = unmanaged.newInstance();

        unmanagedInstance.produce().postConstruct().inject().get();
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == MessageEndpoint.class) {
                if (method.getName().equals("release")) {
                    unmanagedInstance.preDestroy().dispose();
                }
            } else {
                return method.invoke(unmanagedInstance.get(), args);
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
}
