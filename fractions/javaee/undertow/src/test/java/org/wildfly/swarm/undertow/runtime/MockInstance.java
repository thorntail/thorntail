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
