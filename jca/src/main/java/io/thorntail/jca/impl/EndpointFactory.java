package io.thorntail.jca.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import io.thorntail.ActiveInstance;
import io.thorntail.Thorntail;

/**
 * Created by bob on 2/8/18.
 */
public class EndpointFactory implements MessageEndpointFactory {

    public EndpointFactory(Thorntail system, Class<?> instanceType, Class<?> listenerInterface) {
        this.system = system;
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
        ActiveInstance<?> instance = this.system.instance(this.instanceType);

        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == MessageEndpoint.class) {
                if (method.getName().equals("release")) {
                    instance.release();
                } else if ( method.getName().equals("beforeDelivery" ) ) {

                } else if ( method.getName().equals("afterDelivery" ) ) {

                }
            } else {
                return method.invoke(instance.get(), args);
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

    private final Thorntail system;
}
