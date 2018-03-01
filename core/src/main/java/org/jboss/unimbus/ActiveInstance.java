package org.jboss.unimbus;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.SubclassDecoratorApplyingInstantiator;

/**
 * An activated but unmanaged instance.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 * @see UNimbus#activate(Object)
 */
public class ActiveInstance<T> {

    ActiveInstance(String containerId, T object) {
        this(containerId, (Class<T>) object.getClass(), object);
    }

    ActiveInstance(String containerId, Class<T> type, T object) {
        BeanManager manager = CDI.current().getBeanManager();

        AnnotatedType annotatedType = manager.createAnnotatedType(type);
        BeanInjectionTarget injectionTarget = (BeanInjectionTarget) manager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
        this.bean = new InstanceBean<T>(injectionTarget, type, object);

        if (object != null) {
            injectionTarget.setInstantiator(new InstanceInstantiator(object));
        }

        List<Decorator<?>> decorators = manager.resolveDecorators(bean.getTypes(), bean.getQualifiers().toArray(new Annotation[]{}));

        decorators = filter(bean, decorators);

        if (!decorators.isEmpty()) {
            Instantiator instantiator = injectionTarget.getInstantiator();
            injectionTarget.setInstantiator(new SubclassDecoratorApplyingInstantiator(containerId, instantiator, bean, decorators));
        }

        this.context = manager.createCreationalContext(null);
        this.instance = bean.create(context);

    }

    List<Decorator<?>> filter(Bean<?> bean, List<Decorator<?>> decorators) {
        List<Decorator<?>> filtered = new ArrayList<>();

        List<Annotation> beanAnnos = Arrays.asList(bean.getBeanClass().getAnnotations());
        Set<Class<? extends Annotation>> beanAnnoTypes = beanAnnos.stream().map(e -> e.annotationType()).collect(Collectors.toSet());

        for (Decorator<?> decorator : decorators) {
            List<Annotation> decoratorAnnos = Arrays.asList(decorator.getBeanClass().getAnnotations());
            Set<Class<? extends Annotation>> decoratorAnnoTypes = decoratorAnnos.stream().map(e -> e.annotationType()).collect(Collectors.toSet());

            for (Class<? extends Annotation> beanAnnoType : beanAnnoTypes) {
                if ( decoratorAnnoTypes.contains(beanAnnoType ) ) {
                    filtered.add(decorator);
                    break;
                }
            }
        }

        return filtered;
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
     * Release, destroy and dispose of the activated object.
     */
    public void release() {
        this.bean.destroy(this.instance, this.context);
    }

    private final T instance;

    private final CreationalContext<T> context;

    private final InstanceBean<T> bean;
}
