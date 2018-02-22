package org.jboss.unimbus.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for working with annotations.
 */
public class AnnotationUtils {

    /**
     * Will try to peek through proxies to find annotations.
     *
     * @param instance The instance to check.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Object instance, Class<? extends Annotation> annotationType) {
        return hasAnnotation(instance.getClass(), annotationType);
    }

    public static boolean hasAnnotation(Object instance, String annotationTypeName) {
        return hasAnnotation(instance.getClass(), annotationTypeName);
    }

    public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotationType) {
        return getAnnotation(cls, annotationType) != null;
    }

    public static boolean hasAnnotation(Class<?> cls, String annotationTypeName) {
        return getAnnotation(cls, annotationTypeName) != null;
    }

    public static boolean hasAnnotation(Method method, String annotationTypeName) {
        return getAnnotation(method, annotationTypeName) != null;
    }

    public static Annotation getAnnotation(Class<?> cls, Class<? extends Annotation> annotationType) {
        Class<?> cur = cls;

        while (cur != null) {
            Annotation anno = cur.getAnnotation(annotationType);
            if (anno != null) {
                return anno;
            }
            cur = cur.getSuperclass();
        }

        return null;
    }

    public static Annotation getAnnotation(Class<?> cls, String annotationTypeName) {
        Class<?> cur = cls;

        while (cur != null) {
            Annotation anno = getAnnotation(cur.getAnnotations(), annotationTypeName);
            if (anno != null) {
                return anno;
            }
            cur = cur.getSuperclass();
        }

        return null;
    }

    public static Annotation getAnnotation(Method method, String annotationTypeName) {
        Annotation anno = getAnnotation(method.getAnnotations(), annotationTypeName);
        if (anno != null) {
            return anno;
        }
        return null;
    }

    private static Annotation getAnnotation(Annotation[] annos, String annotationTypeName) {
        for (Annotation anno : annos) {
            if (anno.annotationType().getName().equals(annotationTypeName)) {
                return anno;
            }
        }
        return null;
    }

    public static List<Annotation> getAnnotations(Object instance, Class<? extends Annotation> annotationType) {
        List<Annotation> list = new ArrayList<>();

        Class<?> cur = instance.getClass();

        while (cur != null) {
            list.addAll(Arrays.asList(cur.getAnnotationsByType(annotationType)));
            cur = cur.getSuperclass();
        }

        return list;
    }

    public static List<Annotation> getAnnotations(Method method, String annotationTypeName) {
        return filter(method.getAnnotations(), annotationTypeName);
    }

    public static List<Annotation> getAnnotations(Object instance, String annotationTypeName) {
        List<Annotation> list = new ArrayList<>();
        Class<?> cur = instance.getClass();

        while (cur != null) {
            list.addAll( filter( cur.getAnnotations(), annotationTypeName ));
            cur = cur.getSuperclass();
        }

        return list;
    }

    private static List<Annotation> filter(Annotation[] annos, String annotationTypeName) {
        List<Annotation> list = new ArrayList<>();
        for (Annotation anno : annos) {
            if (anno.annotationType().getName().equals(annotationTypeName)) {
                list.add(anno);
            }
        }
        return list;
    }
}
