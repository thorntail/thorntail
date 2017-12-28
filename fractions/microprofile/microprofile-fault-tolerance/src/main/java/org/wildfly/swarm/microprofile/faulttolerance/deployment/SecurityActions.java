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
package org.wildfly.swarm.microprofile.faulttolerance.deployment;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 *
 * @author Martin Kouba
 */
final class SecurityActions {

    private SecurityActions() {
    }

    static Field getDeclaredField(Class<?> clazz, String name) throws NoSuchFieldException, PrivilegedActionException {
        if (System.getSecurityManager() == null) {
            return clazz.getDeclaredField(name);
        }
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Field>() {
            @Override
            public Field run() throws NoSuchFieldException {
                return clazz.getDeclaredField(name);
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

    static void setAccessible(final AccessibleObject accessibleObject) {
        if (System.getSecurityManager() == null) {
            accessibleObject.setAccessible(true);
        }
        AccessController.doPrivileged((PrivilegedAction<AccessibleObject>) () -> {
            accessibleObject.setAccessible(true);
            return accessibleObject;
        });
    }

}
