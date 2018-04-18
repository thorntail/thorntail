package io.thorntail.metrics.impl.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Created by bob on 1/22/18.
 */

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface RegistryTarget {
    MetricRegistry.Type type() default MetricRegistry.Type.APPLICATION;
}
