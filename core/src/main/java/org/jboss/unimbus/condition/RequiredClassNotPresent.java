package org.jboss.unimbus.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Repeatable(MultipleRequiredClassNotPresent.class)
public @interface RequiredClassNotPresent {
    @Nonbinding
    String value() default "";
}
