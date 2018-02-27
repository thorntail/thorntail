package org.jboss.unimbus.opentracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.unimbus.condition.annotation.RequiredClassNotPresent;

/**
 * Annotation which may be placed upon a {@code @Decorator} to signal it can handle a type of tracing interception.
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 *
 * @see RequiredClassNotPresent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface TracingDecorator {
    Class[] value();
}
