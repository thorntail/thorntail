package org.wildfly.swarm.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

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
            //TODO Add the fraction instance as a Bean to the container
//            abd.addBean(new Bean<Object>() {
//                @Override
//                public Object create(CreationalContext<Object> creationalContext) {
//                    return null;
//                }
//
//                @Override
//                public void destroy(Object instance, CreationalContext<Object> creationalContext) {
//
//                }
//
//                @Override
//                public Set<Type> getTypes() {
//                    return null;
//                }
//
//                @Override
//                public Set<Annotation> getQualifiers() {
//                    return null;
//                }
//
//                @Override
//                public Class<? extends Annotation> getScope() {
//                    return null;
//                }
//
//                @Override
//                public String getName() {
//                    return null;
//                }
//
//                @Override
//                public Set<Class<? extends Annotation>> getStereotypes() {
//                    return null;
//                }
//
//                @Override
//                public boolean isAlternative() {
//                    return false;
//                }
//
//                @Override
//                public Class<?> getBeanClass() {
//                    return null;
//                }
//
//                @Override
//                public Set<InjectionPoint> getInjectionPoints() {
//                    return null;
//                }
//
//                @Override
//                public boolean isNullable() {
//                    return false;
//                }
//            });
        }
    }
}
