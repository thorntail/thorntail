package io.thorntail;

import java.lang.reflect.Constructor;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Created by bob on 2/22/18.
 */
class InstanceInstantiator<T> implements Instantiator<T> {

    InstanceInstantiator(T object) {
        this.object = object;
    }

    @Override
    public T newInstance(CreationalContext ctx, BeanManagerImpl manager) {
        return this.object;
    }

    @Override
    public boolean hasInterceptorSupport() {
        return false;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return false;
    }

    @Override
    public Constructor<T> getConstructor() {
        return null;
    }

    private final T object;
}
