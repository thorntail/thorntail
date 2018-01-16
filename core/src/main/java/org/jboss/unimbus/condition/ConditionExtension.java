package org.jboss.unimbus.condition;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

public class ConditionExtension implements Extension {

    public <T> void ifClassPresent(@WithAnnotations({IfClassPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(IfClassPresent.class)) {
            Set<IfClassPresent> annos = type.getAnnotations(IfClassPresent.class);

            for (IfClassPresent each : annos) {
                String className = each.value();
                try {
                    type.getJavaClass().getClassLoader().loadClass(className);
                } catch (Throwable t) {
                    allowed = false;
                    break;
                }
            }
        }

        if ( ! allowed ) {
            event.veto();
        }
    }

    public <T> void ifClassNotPresent(@WithAnnotations({IfClassNotPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(IfClassNotPresent.class)) {
            Set<IfClassNotPresent> annos = type.getAnnotations(IfClassNotPresent.class);

            for (IfClassNotPresent each : annos) {
                String className = each.value();
                try {
                    type.getJavaClass().getClassLoader().loadClass(className);
                    allowed = false;
                    break;
                } catch (Throwable t) {
                    // ignore
                }
            }
        }

        if ( ! allowed ) {
            event.veto();
        }
    }
}
