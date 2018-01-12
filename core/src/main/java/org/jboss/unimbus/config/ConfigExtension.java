package org.jboss.unimbus.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.configurator.BeanConfigurator;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class ConfigExtension implements Extension {

    public void processInjectionPoint(@Observes ProcessInjectionPoint<?, ?> event) {
        InjectionPoint ip = event.getInjectionPoint();
        Annotated annotated = ip.getAnnotated();
        if (annotated.isAnnotationPresent(ConfigProperty.class)) {
            this.types.add(ip.getType());
        }
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        AnnotatedType<ConfigExtension> annotatedType = beanManager.createAnnotatedType(ConfigExtension.class);
        AnnotatedMethod<? super ConfigExtension> producerMethod = annotatedType.getMethods().stream()
                .filter(m -> m.getJavaMember().getName().equals("produceConfigurationValue"))
                .findFirst()
                .get();

        BeanAttributes<?> producerAttributes = beanManager.createBeanAttributes(producerMethod);

        for (Type type : types) {
            BeanAttributes<Object> attributes = new DelegatingBeanAttributes<Object>(producerAttributes) {
                @Override
                public Set<Type> getTypes() {
                    final Set<Type> types = new HashSet<>();
                    types.add(Object.class);
                    types.add(type);
                    return types;
                }
            };

            Bean<Object> producerBean = beanManager.createBean(attributes,
                                                               ConfigExtension.class,
                                                               beanManager.getProducerFactory(
                                                                       producerMethod, null

                                                               ));
            event.addBean(producerBean);
        }
    }

    @ConfigProperty
    @Dependent
    private static final Object produceConfigurationValue(final InjectionPoint injectionPoint) {
        String name = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class).name();
        Class<?> type = (Class<?>) injectionPoint.getType();
        return ConfigProvider.getConfig().getValue(name, type);
    }

    private Set<Type> types = new HashSet<>();

    static class DelegatingBeanAttributes<T> implements BeanAttributes<T> {

        private final BeanAttributes<?> delegate;

        public DelegatingBeanAttributes(final BeanAttributes<?> delegate) {
            super();
            Objects.requireNonNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return this.delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return this.delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return this.delegate.getScope();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return this.delegate.getStereotypes();
        }

        @Override
        public Set<Type> getTypes() {
            return this.delegate.getTypes();
        }

        @Override
        public boolean isAlternative() {
            return this.delegate.isAlternative();
        }

        @Override
        public String toString() {
            return this.delegate.toString();
        }

    }
}
