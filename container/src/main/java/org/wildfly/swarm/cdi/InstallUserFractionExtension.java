package org.wildfly.swarm.cdi;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.inject.spi.builder.BeanConfigurator;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.DefaultFraction;
import org.wildfly.swarm.spi.api.Fraction;

/**
 * @author Ken Finnigan
 */
public class InstallUserFractionExtension implements Extension {
    public static Map<Class<?>, Fraction> userFractions = new HashMap<>();

    <T> void processAnnotatedType(@Observes @WithAnnotations(DefaultFraction.class) ProcessAnnotatedType<T> pat) {
        if (userFractions.containsKey(pat.getAnnotatedType().getJavaClass())) {
            pat.veto();
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        for (Fraction fraction : userFractions.values()) {
            BeanConfigurator<Object> configurator = abd.addBean()
                    .addType(fraction.getClass())
                    .addType(Fraction.class)
                    .scope(Singleton.class)
                    .produceWith(() -> fraction);

            Annotation[] qualifiers = new Annotation[fraction.getClass().getAnnotations().length - 1];
            int index = 0;
            for (Annotation annotation : fraction.getClass().getAnnotations()) {
                if (!annotation.annotationType().equals(DefaultFraction.class)) {
                    qualifiers[index++] = annotation;
                }
            }
            configurator.qualifiers(qualifiers);
        }
    }
}
