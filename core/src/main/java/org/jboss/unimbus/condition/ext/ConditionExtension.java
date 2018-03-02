package org.jboss.unimbus.condition.ext;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.jboss.unimbus.condition.annotation.MultipleRequiredClassNotPresent;
import org.jboss.unimbus.condition.annotation.MultipleRequiredClassPresent;
import org.jboss.unimbus.condition.annotation.RequiredClassNotPresent;
import org.jboss.unimbus.condition.annotation.RequiredClassPresent;

public class ConditionExtension implements Extension {

    public <T> void ifClassPresent(@WithAnnotations({MultipleRequiredClassPresent.class, RequiredClassPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();

        if (type.isAnnotationPresent(MultipleRequiredClassPresent.class)) {
            Set<MultipleRequiredClassPresent> annos = type.getAnnotations(MultipleRequiredClassPresent.class);

            for (MultipleRequiredClassPresent mrcp : annos) {
                for (RequiredClassPresent each : mrcp.value()) {
                    String className = each.value();
                    try {
                        type.getJavaClass().getClassLoader().loadClass(className);
                    } catch (Throwable t) {
                        allowed = false;
                        break;
                    }
                }
            }
        } else if (type.isAnnotationPresent(RequiredClassPresent.class)) {
            Set<RequiredClassPresent> annos = type.getAnnotations(RequiredClassPresent.class);

            for (RequiredClassPresent each : annos) {
                String className = each.value();
                try {
                    type.getJavaClass().getClassLoader().loadClass(className);
                } catch (Throwable t) {
                    allowed = false;
                    break;
                }
            }
        }

        if (!allowed) {
            event.veto();
        }
    }

    public <T> void ifClassNotPresent(@WithAnnotations({MultipleRequiredClassNotPresent.class, RequiredClassNotPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(MultipleRequiredClassNotPresent.class)) {
            Set<MultipleRequiredClassNotPresent> annos = type.getAnnotations(MultipleRequiredClassNotPresent.class);

            for (MultipleRequiredClassNotPresent mrcnp : annos) {
                for (RequiredClassNotPresent each : mrcnp.value()) {
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
        } else if (type.isAnnotationPresent(RequiredClassNotPresent.class)) {
            Set<RequiredClassNotPresent> annos = type.getAnnotations(RequiredClassNotPresent.class);

            for (RequiredClassNotPresent each : annos) {
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

        if (!allowed) {
            event.veto();
        }
    }
}
