package io.thorntail;

import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.injection.InterceptionFactoryImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.manager.api.WeldManager;

/**
 * An activated but unmanaged instance.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 * @see Thorntail#activate(Object)
 */
public class ActiveInstance<T> implements AutoCloseable {

    @SuppressWarnings("unchecked")
    ActiveInstance(String containerId, T object) {
        this(containerId, (Class<T>) object.getClass(), object);
    }

    ActiveInstance(String containerId, Class<T> type, T object) {
        WeldManager weldManager = (WeldManager) WeldContainer.instance(containerId).getBeanManager();
        creationalContext = weldManager.createCreationalContext(null);
        AnnotatedType<T> annotatedType = weldManager.createAnnotatedType(type);
        if (object == null) {
            injectionTarget = weldManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
            object = injectionTarget.produce(creationalContext);
        } else {
            injectionTarget = weldManager.getInjectionTargetFactory(annotatedType).createNonProducibleInjectionTarget();
        }
        injectionTarget.inject(object, creationalContext);
        injectionTarget.postConstruct(object);
        // If there are no interceptors the original instance is used
        instance = InterceptionFactoryImpl.of(BeanManagerProxy.unwrap(weldManager), creationalContext, annotatedType).createInterceptedInstance(object);
    }

    /**
     * Retrieve the activated object.
     *
     * @return The activated object.
     */
    public T get() {
        return this.instance;
    }

    /**
     * Calls {@link PreDestroy} callback and destroys all dependent objects of the activated object.
     */
    public void release() {
        injectionTarget.preDestroy(instance);
        creationalContext.release();
    }

    @Override
    public void close() {
        release();
    }

    private final T instance;

    private final CreationalContext<T> creationalContext;

    private final WeldInjectionTarget<T> injectionTarget;

}
