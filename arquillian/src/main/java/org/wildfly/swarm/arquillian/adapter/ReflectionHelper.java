/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.swarm.arquillian.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SecurityActions
 *
 * A set of privileged actions that are not to leak out of this package
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public final class ReflectionHelper {

    // -------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * No instantiation
     */
    private ReflectionHelper() {
        throw new UnsupportedOperationException("No instantiation");
    }

    // -------------------------------------------------------------------------------||
    // Utility Methods --------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * Obtains the Thread Context ClassLoader
     */
    public static ClassLoader getThreadContextClassLoader() {
        return AccessController.doPrivileged(GetTcclAction.INSTANCE);
    }

    /**
     * Obtains the Constructor specified from the given Class and argument types
     *
     * @param clazz
     * @param argumentTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... argumentTypes)
            throws NoSuchMethodException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<?>>() {
                @Override
                public Constructor<?> run() throws NoSuchMethodException {
                    return clazz.getConstructor(argumentTypes);
                }
            });
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) t;
            } else {
                // No other checked Exception thrown by Class.getConstructor
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }

    public static <T> Constructor<T> getAssignableConstructor(final Class<T> clazz, final Class<?>... argumentTypes) throws NoSuchMethodException {
        for (Constructor constructor: clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length != argumentTypes.length) {
                continue;
            }
            boolean found = true;
            for (int i=0; i<argumentTypes.length; i++) {
                if (!constructor.getParameterTypes()[i].isAssignableFrom(argumentTypes[i])) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("There is no constructor in class " + clazz.getName() + " with arguments " + Arrays.toString(argumentTypes));
    }

    public static boolean hasConstructor(final Class<?> clazz, final Class<?>... argumentTypes) {
        try {
            clazz.getDeclaredConstructor(argumentTypes);
            return true;
        } catch(NoSuchMethodException ignored) {
            return false;
        }
    }

    /**
     * Create a new instance by finding a constructor that matches the argumentTypes signature using the arguments for
     * instantiation.
     *
     * @param className Full classname of class to create
     * @param argumentTypes The constructor argument types
     * @param arguments The constructor arguments
     * @return a new instance
     * @throws IllegalArgumentException if className, argumentTypes, or arguments are null
     * @throws RuntimeException if any exceptions during creation
     * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
     * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
     */
    public static <T> T newInstance(final String className, final Class<?>[] argumentTypes, final Object[] arguments,
                                    final Class<T> expectedType) {
        if (className == null) {
            throw new IllegalArgumentException("ClassName must be specified");
        }
        if (argumentTypes == null) {
            throw new IllegalArgumentException("ArgumentTypes must be specified. Use empty array if no arguments");
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments must be specified. Use empty array if no arguments");
        }
        final Object obj;
        try {
            final ClassLoader tccl = getThreadContextClassLoader();
            final Class<?> implClass = Class.forName(className, false, tccl);
            Constructor<?> constructor = getConstructor(implClass, argumentTypes);
            obj = constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Could not create new instance of " + className + ", missing package from classpath?", e);
        }

        // Cast
        try {
            return expectedType.cast(obj);
        } catch (final ClassCastException cce) {
            // Reconstruct so we get some useful information
            throw new ClassCastException("Incorrect expected type, " + expectedType.getName() + ", defined for "
                    + obj.getClass().getName());
        }
    }

    public static boolean isClassPresent(String name) {
        try {
            ClassLoader classLoader = getThreadContextClassLoader();
            classLoader.loadClass(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static List<Field> getFields(final Class<?> source) {
        return AccessController.doPrivileged(new PrivilegedAction<List<Field>>() {
            @Override
            public List<Field> run() {
                List<Field> foundFields = new ArrayList<Field>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        foundFields.add(field);
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            }
        });
    }

    public static List<Field> getFieldsWithAnnotation(final Class<?> source, final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController.doPrivileged(new PrivilegedAction<List<Field>>() {
            @Override
            public List<Field> run() {
                List<Field> foundFields = new ArrayList<Field>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotationClass)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            foundFields.add(field);
                        }
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            }
        });
        return declaredAccessableFields;
    }

    public static List<Field> getFieldsWithAnnotatedAnnotation(final Class<?> source, final Class<? extends Annotation> annotationClass) {
        List<Field> declaredAccessableFields = AccessController.doPrivileged(new PrivilegedAction<List<Field>>() {
            @Override
            public List<Field> run() {
                List<Field> foundFields = new ArrayList<Field>();
                Class<?> nextSource = source;
                while (nextSource != Object.class) {
                    for (Field field : nextSource.getDeclaredFields()) {
                        for (Annotation annotation : field.getAnnotations()) {
                            if (annotation.annotationType().isAnnotationPresent(annotationClass)) {
                                if (!field.isAccessible()) {
                                    field.setAccessible(true);
                                }
                                foundFields.add(field);
                                break;
                            }
                        }
                    }
                    nextSource = nextSource.getSuperclass();
                }
                return foundFields;
            }
        });
        return declaredAccessableFields;
    }

    public static List<Method> getMethodsWithAnnotation(final Class<?> source, final Class<? extends Annotation> annotationClass) {
        List<Method> declaredAccessableMethods = AccessController.doPrivileged(new PrivilegedAction<List<Method>>() {
            @Override
            public List<Method> run() {
                List<Method> foundMethods = new ArrayList<Method>();
                for (Method method : source.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        foundMethods.add(method);
                    }
                }
                return foundMethods;
            }
        });
        return declaredAccessableMethods;
    }

    // -------------------------------------------------------------------------------||
    // Inner Classes ----------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * Single instance to get the TCCL
     */
    private enum GetTcclAction implements PrivilegedAction<ClassLoader> {
        INSTANCE;

        @Override
        public ClassLoader run() {
            return Thread.currentThread().getContextClassLoader();
        }

    }

}