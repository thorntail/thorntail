package org.jboss.unimbus.jndi;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bob on 1/16/18.
 */
public abstract class Binder<T> {

    public Binder(String name) {
        this.name = name;
    }

    abstract public T produce() throws Exception;

    public T get() {
        return this.ref.updateAndGet( (orig)->{
            if ( orig !=  null ) {
                return orig;
            }
            try {
                return produce();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public String getName() {
        return this.name;
    }

    private final String name;
    private AtomicReference<T> ref = new AtomicReference<T>();
}
