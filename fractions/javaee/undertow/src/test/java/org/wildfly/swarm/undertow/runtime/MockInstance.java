/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.undertow.runtime;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.naming.OperationNotSupportedException;

/**
 * @author Bob McWhirter
 */
public class MockInstance<T> implements Instance<T> {

    private final T instance;

    public MockInstance(T instance) {
        this.instance = instance;
    }

    @Override
    public Instance<T> select(Annotation... annotations) {
        throw new RuntimeException("operation not supported by mock");
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
        throw new RuntimeException("operation not supported by mock");
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        throw new RuntimeException("operation not supported by mock");
    }

    @Override
    public boolean isUnsatisfied() {
        return this.instance == null;
    }

    @Override
    public boolean isAmbiguous() {
        return false;
    }

    @Override
    public void destroy(T t) {

    }

    @Override
    public Iterator<T> iterator() {
        if ( this.instance != null ) {
            return Collections.singleton(this.instance).iterator();
        }

        return new ArrayList<T>().iterator();
    }

    @Override
    public T get() {
        return this.instance;
    }
}
