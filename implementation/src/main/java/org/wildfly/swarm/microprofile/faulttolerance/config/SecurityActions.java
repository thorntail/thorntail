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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 *
 * @author Martin Kouba
 */
final class SecurityActions {

    static Method getAnnotationMethod(Class<?> clazz, String name) throws PrivilegedActionException, NoSuchMethodException {
        if (System.getSecurityManager() == null) {
            return clazz.getMethod(name);
        }
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
            @Override
            public Method run() throws NoSuchMethodException, SecurityException {
                return clazz.getMethod(name);
            }
        });
    }

    static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>[] parameterTypes) throws NoSuchMethodException, PrivilegedActionException {
        if (System.getSecurityManager() == null) {
            return clazz.getDeclaredMethod(name, parameterTypes);
        }
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {
            @Override
            public Method run() throws NoSuchMethodException {
                return clazz.getDeclaredMethod(name, parameterTypes);
            }
        });
    }

}
