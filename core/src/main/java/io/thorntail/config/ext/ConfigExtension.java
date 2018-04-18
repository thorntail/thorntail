package io.thorntail.config.ext;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.thorntail.config.impl.ConfigImpl;

public class ConfigExtension implements Extension {

    public void processInjectionPoint(@Observes ProcessInjectionPoint<?, ?> event) {
        InjectionPoint ip = event.getInjectionPoint();
        Annotated annotated = ip.getAnnotated();
        if (annotated.isAnnotationPresent(ConfigProperty.class)) {
            Injection injection = createInjection(ip);
            injections.add(injection);
        }
    }

    static Injection createInjection(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        if (annotated.isAnnotationPresent(ConfigProperty.class)) {
            ConfigProperty anno = annotated.getAnnotation(ConfigProperty.class);

            String defaultValue = anno.defaultValue();
            if (defaultValue.equals(ConfigProperty.UNCONFIGURED_VALUE)) {
                defaultValue = null;
            }

            return new Injection(
                    determineName(ip),
                    normalizePrimitiveType(ip.getType()),
                    defaultValue
            );
        }

        return null;
    }

    static String determineName(InjectionPoint injectionPoint) {
        ConfigProperty anno = injectionPoint.getAnnotated().getAnnotation(ConfigProperty.class);
        if (!anno.name().equals("")) {
            return anno.name();
        }

        return (injectionPoint.getMember().getDeclaringClass().getName() + "." + injectionPoint.getMember().getName()).replace('$', '.');
    }

    public void validate(@Observes AfterDeploymentValidation event) {
        ConfigImpl config = (ConfigImpl) ConfigProvider.getConfig();

        this.injections.forEach(each -> {
            try {
                Object result = each.produce(config);
            } catch (Throwable t) {
                event.addDeploymentProblem(t);
            }
        });
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        AnnotatedType<ConfigExtension> annotatedType = beanManager.createAnnotatedType(ConfigExtension.class);
        AnnotatedMethod<? super ConfigExtension> producerMethod = annotatedType.getMethods().stream()
                .filter(m -> m.getJavaMember().getName().equals("produceConfigurationValue"))
                .findFirst()
                .get();

        BeanAttributes<?> producerAttributes = beanManager.createBeanAttributes(producerMethod);

        normalizedTypes().forEach((type) -> {
            BeanAttributes<Object> attributes = new ConfigPropertyBeanAttributes<Object>(producerAttributes, type);
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
                .map(e -> e.getInjectionType())
                .map(e -> normalizePrimitiveType(e))
                .distinct();
    }

    static Type normalizePrimitiveType(Type in) {
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
    private static final Object produceConfigurationValue(final InjectionPoint ip) throws IllegalAccessException, InstantiationException {
        Injection injection = createInjection(ip);
        Object result = injection.produce((ConfigImpl) ConfigProvider.getConfig());
        return result;
    }


    private List<Injection> injections = new ArrayList<>();
}
