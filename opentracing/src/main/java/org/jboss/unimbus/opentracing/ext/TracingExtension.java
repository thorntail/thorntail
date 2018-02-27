package org.jboss.unimbus.opentracing.ext;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.unimbus.opentracing.TracingDecorator;

/**
 * Extension that looks for any @Decorator that is also @Traced.
 */
public class TracingExtension implements Extension {

    public <T> void discoverDecorators(@Observes @WithAnnotations({TracingDecorator.class}) ProcessAnnotatedType<T> event) {
        if (event.getAnnotatedType().isAnnotationPresent(Traced.class)) {
            TracingDecorator anno = event.getAnnotatedType().getAnnotation(TracingDecorator.class);
            for (Class each : anno.value()) {
                this.handledTypes.add(each);
            }
        }
    }


    public boolean canHandle(Set<Type> types) {
        return this.handledTypes.stream().anyMatch(types::contains);
    }

    private Set<Type> handledTypes = new HashSet<>();
}
