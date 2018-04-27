package io.thorntail.condition.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holder for repeatable annotation {@link RequiredClassNotPresent}
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 *
 * @see RequiredClassNotPresent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface MultipleRequiredClassNotPresent {
    RequiredClassNotPresent[] value();
}
