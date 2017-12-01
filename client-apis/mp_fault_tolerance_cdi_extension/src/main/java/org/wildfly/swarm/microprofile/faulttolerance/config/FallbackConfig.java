/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.faulttolerance.config;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */
public class FallbackConfig extends GenericConfig<Fallback> {

    public static final String VALUE = "value";

    public static final String FALLBACK_METHOD = "fallbackMethod";

    public FallbackConfig(Method method) {
        super(Fallback.class, method);
    }

    public FallbackConfig(AnnotatedMethod<?> annotatedMethod) {
        super(Fallback.class, annotatedMethod);
    }

    @Override
    public void validate() {
        if (!"".equals(get(FALLBACK_METHOD))) {
            if (!Fallback.DEFAULT.class.equals(get(VALUE))) {
                throw new FaultToleranceDefinitionException("Fallback configuration can't contain an handler class and method at the same time");
            }
            Method fallbackMethod;
            try {
                fallbackMethod = SecurityActions.getDeclaredMethod(method.getDeclaringClass(), get(FALLBACK_METHOD), method.getParameterTypes());
            } catch (NoSuchMethodException | PrivilegedActionException e) {
                throw new FaultToleranceDefinitionException(
                        "Fallback method " + get(FALLBACK_METHOD) + " with same parameters as " + method.getName() + " not found", e);
            }
            if (!isAssignableFrom(method.getGenericReturnType(), fallbackMethod.getGenericReturnType())) {
                throw new FaultToleranceDefinitionException(
                        "Fallback method " + get(FALLBACK_METHOD) + " must have a return type assignable to " + method.getName());
            }
        }
        if (!Fallback.DEFAULT.class.equals(get(VALUE))) {
            Class<?> fbhc = get(VALUE);
            Type fallbackType = null;
            for (Type genericInterface : fbhc.getGenericInterfaces()) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    if (parameterizedType.getRawType().equals(FallbackHandler.class)) {
                        fallbackType = parameterizedType.getActualTypeArguments()[0];
                        break;
                    }
                }
            }
            if (fallbackType == null || !method.getGenericReturnType().equals(fallbackType)) {
                throw new FaultToleranceDefinitionException(
                        "Fallback handler type [" + fallbackType + "] is not the same as the method return type: " + method);
            }
        }
    }

    @Override
    protected Class<Fallback> getConfigType() {
        return Fallback.class;
    }

    @Override
    protected Map<String, Class<?>> getKeysToType() {
        return keys2Type;
    }

    private static Map<String, Class<?>> keys2Type = initKeys();

    private static Map<String, Class<?>> initKeys() {
        Map<String, Class<?>> keys = new HashMap<>();
        keys.put(VALUE, Class.class);
        keys.put(FALLBACK_METHOD, String.class);
        return Collections.unmodifiableMap(keys);
    }

    /**
     * The assignability checks are incomplete and need revision.
     *
     * @param type1
     * @param type2
     * @return {@code true} if type1 is assignable from type2
     */
    private static boolean isAssignableFrom(Type type1, Type type2) {
        if (type1 instanceof Class<?>) {
            if (type2 instanceof Class<?>) {
                return isAssignableFrom((Class<?>) type1, (Class<?>) type2);
            }
            if (type2 instanceof ParameterizedType) {
                return isAssignableFrom((Class<?>) type1, (ParameterizedType) type2);
            }
            throw new IllegalArgumentException("Unsupported type " + type2);
        }
        if (type1 instanceof ParameterizedType) {
            if (type2 instanceof ParameterizedType) {
                return isAssignableFrom((ParameterizedType) type1, (ParameterizedType) type2);
            }
            throw new IllegalArgumentException("Unsupported type " + type2);
        }
        throw new IllegalArgumentException("Unsupported type " + type1);
    }

    private static boolean isAssignableFrom(Class<?> type1, Class<?> type2) {
        return type1.isAssignableFrom(type2);
    }

    private static boolean isAssignableFrom(ParameterizedType type1, ParameterizedType type2) {
        final Class<?> rawType1 = (Class<?>) type1.getRawType();
        final Class<?> rawType2 = (Class<?>) type2.getRawType();
        if (!rawType1.equals(rawType2)) {
            return false;
        }
        final Type[] types1 = type1.getActualTypeArguments();
        final Type[] types2 = type2.getActualTypeArguments();
        if (types1.length != types2.length) {
            return false;
        }
        for (int i = 0; i < type1.getActualTypeArguments().length; i++) {
            if (!isAssignableFrom(types1[i], types2[i])) {
                return false;
            }
        }
        return true;
    }

}