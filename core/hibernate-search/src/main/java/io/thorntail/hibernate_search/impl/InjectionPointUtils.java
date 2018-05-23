package io.thorntail.hibernate_search.impl;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author kg6zvp
 */
public class InjectionPointUtils {
    public static <T extends Annotation> T getAnnotation(InjectionPoint ip, Class<T> annotationClass) {
        for (Annotation a : ip.getQualifiers()) {
            if (annotationClass.isAssignableFrom(a.getClass())) {
                return (T) a;
            }
        }
        return null;
    }
}
