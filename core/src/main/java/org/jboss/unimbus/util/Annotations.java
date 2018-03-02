package org.jboss.unimbus.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Qualifier;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.bean.builtin.BeanManagerProxy;

/**
 * Utility class for working with annotations.
 *
 * <p>These utilities will attempt to peer through any proxies that might hide the actual annotations,
 * by crawling the class hierarchy for objects and classes.</p>
 *
 * <p>Additionally, they provide methods for looking for annotations by string class name, instead
 * of requiring the actual annotation class to be on the classpath.</p>
 */
public class Annotations {

    private Annotations() {

    }

    /**
     * Determine if an instance has a given annotation.
     *
     * @param instance       The instance to check.
     * @param annotationType The class of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Object instance, Class<? extends Annotation> annotationType) {
        return hasAnnotation(instance.getClass(), annotationType);
    }

    /**
     * Determine if an instance has a given annotation, by annotation class name.
     *
     * @param instance           The instance to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Object instance, String annotationTypeName) {
        return hasAnnotation(instance.getClass(), annotationTypeName);
    }

    /**
     * Determine if a class has a given annotation.
     *
     * @param cls            The class to check.
     * @param annotationType The class of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Class<?> cls, Class<? extends Annotation> annotationType) {
        return getAnnotation(cls, annotationType) != null;
    }

    /**
     * Determine if a class has a given annotation, by annotation class name.
     *
     * @param cls                The class to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Class<?> cls, String annotationTypeName) {
        return getAnnotation(cls, annotationTypeName) != null;
    }

    /**
     * Determine if a method has a given annotation, by annotation class name.
     *
     * @param method             The method to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static boolean hasAnnotation(Method method, String annotationTypeName) {
        return getAnnotation(method, annotationTypeName) != null;
    }

    /**
     * Retrieve one annotation of a class.
     *
     * @param cls            The class to check.
     * @param annotationType The class of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static <T extends Annotation> T getAnnotation(Class<?> cls, Class<T> annotationType) {
        Class<?> cur = cls;

        while (cur != null) {
            T anno = cur.getAnnotation(annotationType);
            if (anno != null) {
                return anno;
            }
            cur = cur.getSuperclass();
        }

        return null;
    }

    /**
     * Retrieve one annotation of a class, by annotation class name.
     *
     * @param cls                The class to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
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
    public static <T extends Annotation> T getAnnotation(Object instance, Class<T> annotationType) {
        Class<?> cur = instance.getClass();

        while (cur != null) {
            T anno = getAnnotation(cur.getAnnotations(), annotationType);
            if (anno != null) {
                return anno;
            }
            cur = cur.getSuperclass();
        }

        return null;

    }

    /**
     * Retrieve one annotation of a method, by annotation class name.
     *
     * @param method             The class to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
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

    private static <T extends Annotation> T getAnnotation(Annotation[] annos, Class<T> annotationType) {
        for (Annotation anno : annos) {
            if (anno.annotationType().equals(annotationType)) {
                return (T) anno;
            }
        }
        return null;
    }

    /** Retrieve annotations of type of {@code @Qualifier}.
     *
     * @param cls The class to check.
     * @return The set of all annotations of type {@code @Qualifier}
     */
    public static Set<Annotation> getQualifiers(Class<?> cls) {
        Set<Annotation> annotations = new HashSet<>();
        Class<?> cur = cls;

        while ( cur != null ) {
            annotations.addAll( Arrays.asList( cur.getAnnotations()));
            cur = cur.getSuperclass();
        }

        BeanManager beanManager = CDI.current().getBeanManager();

        Set<Annotation> set = new HashSet<>();
        for (Annotation each : annotations) {
            if ( beanManager.isQualifier(each.annotationType())) {
                set.add( each );
            }
            //if (each.annotationType().getAnnotation(Qualifier.class) != null) {
                //set.add(each);
            //}
        }
        return set;
    }

    /** Retrieve annotations of type of {@code @InterceptorBinding}.
     *
     * @param cls The class to check.
     * @return The set of all annotations of type {@code @InterceptorBinding}
     */
    public static Set<Annotation> getInterceptorBindings(Class<?> cls) {
        Set<Annotation> annotations = new HashSet<>();
        Class<?> cur = cls;

        while ( cur != null ) {
            annotations.addAll( Arrays.asList( cur.getAnnotations()));
            cur = cur.getSuperclass();
        }

        Set<Annotation> set = new HashSet<>();
        for (Annotation each : annotations) {
            if (each.annotationType().getAnnotation(InterceptorBinding.class) != null) {
                set.add(each);
            }
        }
        return set;
    }

    /**
     * Retrieve annotations of an object.
     *
     * @param instance       The instance to check.
     * @param annotationType The class of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static List<Annotation> getAnnotations(Object instance, Class<? extends Annotation> annotationType) {
        List<Annotation> list = new ArrayList<>();

        Class<?> cur = instance.getClass();

        while (cur != null) {
            list.addAll(Arrays.asList(cur.getAnnotationsByType(annotationType)));
            cur = cur.getSuperclass();
        }

        return list;
    }

    /**
     * Retrieve annotations of a method, by annotation type name..
     *
     * @param method             The method to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static List<Annotation> getAnnotations(Method method, String annotationTypeName) {
        return filter(method.getAnnotations(), annotationTypeName);
    }

    /**
     * Retrieve annotations of an object, by annotation type name..
     *
     * @param instance       The instance to check.
     * @param annotationTypeName The class name of the annotation.
     * @return {@code true} if the annotation is present, otherwise {@code false}.
     */
    public static List<Annotation> getAnnotations(Object instance, String annotationTypeName) {
        List<Annotation> list = new ArrayList<>();
        Class<?> cur = instance.getClass();

        while (cur != null) {
            list.addAll(filter(cur.getAnnotations(), annotationTypeName));
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
