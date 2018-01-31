package org.jboss.unimbus.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.unimbus.config.impl.ConfigImpl;

public class ConfigExtension implements Extension {

    public void processInjectionPoint(@Observes ProcessInjectionPoint<?, ?> event) {
        InjectionPoint ip = event.getInjectionPoint();
        Annotated annotated = ip.getAnnotated();
        if (annotated.isAnnotationPresent(ConfigProperty.class)) {
            ConfigProperty anno = annotated.getAnnotation(ConfigProperty.class);
            this.injections.add(new Injection(
                    ip,
                    anno.name(),
                    ip.getType(),
                    !anno.defaultValue().equals(ConfigProperty.UNCONFIGURED_VALUE)
            ));
        }
    }

    public void validate(@Observes AfterDeploymentValidation event) {
        Config config = ConfigProvider.getConfig();

        this.injections.forEach(each -> {
            NoSuchElementException result = validate(config, each.injectionPoint, each.name, each.conversionType(), each.hasDefault, each.isOptional());
            if (result != null) {
                event.addDeploymentProblem(result);
            }
        });
    }

    <T> NoSuchElementException validate(Config config, InjectionPoint injectionPoint, String propertyName, Class<T> conversionType, boolean hasDefault, boolean isOptional) {
        String name = determineName(injectionPoint);
        Optional<T> val = config.getOptionalValue(name, conversionType);
        if (val.isPresent() || hasDefault || isOptional) {
            return null;
        }

        return new NoSuchElementException(name + " at " + injectionPoint);
    }

    static String determineName(InjectionPoint injectionPoint) {
        ConfigProperty anno = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
        if (!anno.name().equals("")) {
            return anno.name();
        }

        return (injectionPoint.getMember().getDeclaringClass().getName() + "." + injectionPoint.getMember().getName()).replace('$', '.');
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        AnnotatedType<ConfigExtension> annotatedType = beanManager.createAnnotatedType(ConfigExtension.class);
        AnnotatedMethod<? super ConfigExtension> producerMethod = annotatedType.getMethods().stream()
                .filter(m -> m.getJavaMember().getName().equals("produceConfigurationValue"))
                .findFirst()
                .get();

        BeanAttributes<?> producerAttributes = beanManager.createBeanAttributes(producerMethod);

        //this.injections.stream().map(e -> e.type).forEach((type) -> {
        normalizedTypes().forEach((type) -> {
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
        });
    }

    Stream<Type> normalizedTypes() {
        return this.injections.stream()
                .map(e -> e.type)
                .map(e -> normalizePrimitiveType(e))
                .distinct();
    }

    Type normalizePrimitiveType(Type in) {
        if (!(in instanceof Class)) {
            return in;
        }

        Class<?> cls = (Class<?>) in;
        if (!cls.isPrimitive()) {
            return cls;
        }

        if (cls == double.class) {
            return Double.class;
        }
        if (cls == float.class) {
            return Float.class;
        }
        if (cls == short.class) {
            return Short.class;
        }
        if (cls == int.class) {
            return Integer.class;
        }
        if (cls == long.class) {
            return Long.class;
        }
        if (cls == boolean.class) {
            return Boolean.class;
        }

        return cls;


    }

    @ConfigProperty
    @Dependent
    private static final Object produceConfigurationValue(final InjectionPoint injectionPoint) {
        String name = determineName(injectionPoint);
        Type type = injectionPoint.getType();
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) injectionPoint.getType();
            Optional<?> value = ConfigProvider.getConfig().getOptionalValue(name, cls);
            if (value.isPresent()) {
                return value.get();
            }
            ConfigProperty anno = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
            if (anno != null) {
                String defaultValue = anno.defaultValue();
                if (defaultValue != ConfigProperty.UNCONFIGURED_VALUE) {
                    return ((ConfigImpl) ConfigProvider.getConfig()).convert(defaultValue, cls).get();
                }
            }
        } else if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() == Optional.class) {
                Type innerType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (innerType instanceof Class) {
                    return ConfigProvider.getConfig().getOptionalValue(name, (Class) innerType);
                } else if (innerType instanceof ParameterizedType) {
                    return ConfigProvider.getConfig().getOptionalValue(name, (Class) ((ParameterizedType) innerType).getRawType());
                }
            }
        }
        throw new RuntimeException("unable to resolve property to type: " + type.getTypeName());
    }

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

    static class Injection {
        Injection(InjectionPoint injectionPoint, String name, Type type, boolean hasDefault) {
            this.injectionPoint = injectionPoint;
            this.name = name;
            this.type = type;
            this.hasDefault = hasDefault;
        }

        Class<?> conversionType() {
            if (this.type instanceof Class<?>) {
                return (Class<?>) this.type;
            }
            if (this.type instanceof ParameterizedType) {
                Type innerType = ((ParameterizedType) this.type).getActualTypeArguments()[0];
                if (innerType instanceof Class) {
                    return (Class<?>) innerType;
                } else if (innerType instanceof ParameterizedType) {
                    return (Class<?>) ((ParameterizedType) innerType).getRawType();
                }
            }

            return null;
        }

        boolean isOptional() {
            if (this.type instanceof ParameterizedType) {
                return ((ParameterizedType) this.type).getRawType() == Optional.class;
            }

            return false;
        }

        private final InjectionPoint injectionPoint;

        String name;

        Type type;

        boolean hasDefault;
    }

    private List<Injection> injections = new ArrayList<>();
}
