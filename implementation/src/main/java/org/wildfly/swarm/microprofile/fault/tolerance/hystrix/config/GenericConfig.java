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
package org.wildfly.swarm.microprofile.fault.tolerance.hystrix.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

/**
 * @author Antoine Sabot-Durand
 */

public abstract class GenericConfig<X extends Annotation> {

    public GenericConfig(X annotation, Method method) {
        this.method = method;
        this.annotation = annotation;
        annotated =null;
    }

    public GenericConfig(X annotation, Annotated annotated) {
        if(annotated instanceof AnnotatedMethod) {
            method = ((AnnotatedMethod<?>)annotated).getJavaMember();
        } else {
            method = ((AnnotatedType<?>)annotated).getJavaClass().getMethods()[0];
        }
        this.annotation =annotation;
        this.annotated = annotated;
    }



    public <U> U get(String key, Class<U> expectedType) {

        /*
           Global config has the highest priority
         */
        Optional<U> opt = getConfig().getOptionalValue(getConfigType() + "/" + key, expectedType);
        if (opt.isPresent()) {
            return opt.get();
        }

        /*
            Config on field or on field annotation is priority 2
         */
        if (method.isAnnotationPresent(annotation.annotationType())) {
            opt = getConfig().getOptionalValue(getConfigKeyForMethod() + key, expectedType);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                return getConfigFromAnnotation(key);
            }
        }

        /*
            lowest priority for config on class
         */
        opt = getConfig().getOptionalValue(getConfigKeyForClass() + key, expectedType);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return getConfigFromAnnotation(key);
        }

    }

    public <U> U get(String key) {
        Class<U> expectedType = (Class<U>) getKeysToType().get(key);
        return get(key, expectedType);
    }


    public abstract void validate();

    private <U> U getConfigFromAnnotation(String key) {
        try {
            return (U) annotation.getClass().getMethod(key).invoke(annotation);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new FaultToleranceDefinitionException("Member " + key + " on annotation " + annotation.getClass().toString() + " doesn't exist or is not accessible");
        }
    }

    protected String getConfigKeyForMethod() {
        return method.getDeclaringClass().getName() + "/" + method.getName() + "/" + getConfigType() + "/";
    }

    protected abstract String getConfigType();

    protected String getConfigKeyForClass() {
        return method.getDeclaringClass().getName() + "/" + getConfigType() + "/";
    }

    protected Config getConfig() {
        return ConfigProvider.getConfig();
    }


    protected abstract Map<String, Class<?>> getKeysToType();

    protected final Method method;

    protected final X annotation;

    protected final Annotated annotated;

}
