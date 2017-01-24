package org.wildfly.swarm.container.runtime.cdi.configurable;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.wildfly.swarm.container.runtime.ConfigurableManager;

/**
 * @author Bob McWhirter
 */
public class ConfigurableInjectionTarget<T> implements InjectionTarget<T> {

    private final InjectionTarget<T> delegate;

    private final ConfigurableManager configurableManager;

    public ConfigurableInjectionTarget(InjectionTarget<T> delegate, ConfigurableManager configurableManager) {
        this.delegate = delegate;
        this.configurableManager = configurableManager;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        this.delegate.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance) {
        this.delegate.postConstruct(instance);
        try {
            this.configurableManager.scan(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preDestroy(T instance) {
        this.delegate.preDestroy(instance);
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        return this.delegate.produce(ctx);
    }

    @Override
    public void dispose(T instance) {
        this.delegate.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return this.delegate.getInjectionPoints();
    }
}
