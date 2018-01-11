package org.jboss.unimbus.condition;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class ConditionExtension implements Extension {

    public void perform(@Observes ProcessAnnotatedType<?> event) {
        boolean allowed = true;

        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(IfClassPresent.class)) {
            Set<IfClassPresent> annos = type.getAnnotations(IfClassPresent.class);

            for (IfClassPresent each : annos) {
                String className = each.value();
                //System.err.println( "each: " + each.value());
                try {
                    type.getJavaClass().getClassLoader().loadClass(className);
                } catch (Throwable t) {
                    allowed = false;
                }
            }
        }

        if ( ! allowed ) {
            event.veto();
        }
    }
}
