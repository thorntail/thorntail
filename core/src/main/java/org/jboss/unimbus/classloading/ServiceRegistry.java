package org.jboss.unimbus.classloading;

/**
 * Created by bob on 2/20/18.
 */
public interface ServiceRegistry {
    void register(Class<?> serviceInterface, Class<?> implementationClass);
}
