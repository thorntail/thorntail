package org.wildfly.swarm.container.runtime.cdi;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
public class DeploymentContextImpl implements DeploymentContext {

    // It's a normal scope so there may be no more than one mapped instance per contextual type per thread
    private final ThreadLocal<Map<Contextual<?>, ContextualInstance<?>>> currentContext = new ThreadLocal<>();

    private final ThreadLocal<Archive> currentArchive = new ThreadLocal<>();

    public Class<? extends Annotation> getScope() {
        return DeploymentScoped.class;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();

        if (ctx == null) {
            // Thread local not set - context is not active!
            throw new ContextNotActiveException();
        }

        ContextualInstance<T> instance = (ContextualInstance<T>) ctx.get(contextual);

        if (instance == null && creationalContext != null) {
            // Bean instance does not exist - create one if we have CreationalContext
            instance = new ContextualInstance<T>(contextual.create(creationalContext), creationalContext, contextual);
            ctx.put(contextual, instance);
        }

        return instance != null ? instance.get() : null;
    }

    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    public boolean isActive() {
        return currentContext.get() != null;
    }

    public void destroy(Contextual<?> contextual) {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();
        if (ctx == null) {
            return;
        }
        ctx.remove(contextual);
    }

    public void activate(Archive archive) {
        currentContext.set(new HashMap<>());
        currentArchive.set(archive);
    }

    public void deactivate() {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();
        if (ctx == null) {
            return;
        }
        for (ContextualInstance<?> instance : ctx.values()) {
            try {
                instance.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ctx.clear();
        currentContext.remove();
        currentArchive.set(null);
    }

    public Archive getCurrentArchive() {
        return currentArchive.get();
    }

    /**
     * We use this injectable version to detect the original "activator", so that we can skip deactivation during {@link #deactivate()} if needed.
     *
     * @author Martin Kouba
     */
    static final class InjectableDeploymentContext implements DeploymentContext {

        private static final Logger LOGGER = Logger.getLogger(InjectableDeploymentContext.class.getName());

        private final DeploymentContext delegate;

        private final BeanManager beanManager;

        private boolean isActivator;

        /**
         * @param delegate
         * @param beanManager
         */
        InjectableDeploymentContext(DeploymentContext delegate, BeanManager beanManager) {
            this.delegate = delegate;
            this.beanManager = beanManager;
            this.isActivator = false;
        }

        @Override
        public void destroy(Contextual<?> contextual) {
            delegate.destroy(contextual);
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return delegate.getScope();
        }

        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            return delegate.get(contextual);
        }

        @Override
        public <T> T get(Contextual<T> contextual) {
            return delegate.get(contextual);
        }

        @Override
        public boolean isActive() {
            return delegate.isActive();
        }

        @Override
        public void activate(Archive archive) {
            try {
                beanManager.getContext(delegate.getScope());
                LOGGER.info("Command context already active");
            } catch (ContextNotActiveException e) {
                // Only activate the context if not already active
                delegate.activate(archive);
                isActivator = true;
            }
        }

        @Override
        public void deactivate() {
            if (isActivator) {
                delegate.deactivate();
            } else {
                LOGGER.info("Command context not activated by this bean");
            }
        }

        @Override
        public Archive getCurrentArchive() {
            return this.delegate.getCurrentArchive();
        }
    }

    /**
     * This wrapper allows to create and destroy a bean instance properly.
     *
     * @param <T>
     * @author Martin Kouba
     */
    static final class ContextualInstance<T> {

        private final T value;

        private final CreationalContext<T> creationalContext;

        private final Contextual<T> contextual;

        /**
         * @param instance
         * @param creationalContext
         * @param contextual
         */
        ContextualInstance(T instance, CreationalContext<T> creationalContext, Contextual<T> contextual) {
            this.value = instance;
            this.creationalContext = creationalContext;
            this.contextual = contextual;
        }

        T get() {
            return value;
        }

        Contextual<T> getContextual() {
            return contextual;
        }

        void destroy() {
            contextual.destroy(value, creationalContext);
        }

    }

}
