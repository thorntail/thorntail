package io.thorntail.condition.ext;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.condition.annotation.RequiredClassNotPresent;

public class ConditionExtension implements Extension {

    public <T> void ifClassPresent(@WithAnnotations({RequiredClassPresent.List.class, RequiredClassPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();

        if (type.isAnnotationPresent(RequiredClassPresent.List.class)) {
            Set<RequiredClassPresent.List> annos = type.getAnnotations(RequiredClassPresent.List.class);

            for (RequiredClassPresent.List mrcp : annos) {
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

    public <T> void ifClassNotPresent(@WithAnnotations({RequiredClassNotPresent.List.class, RequiredClassNotPresent.class}) @Observes ProcessAnnotatedType<T> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(RequiredClassNotPresent.List.class)) {
            Set<RequiredClassNotPresent.List> annos = type.getAnnotations(RequiredClassNotPresent.List.class);

            for (RequiredClassNotPresent.List mrcnp : annos) {
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
