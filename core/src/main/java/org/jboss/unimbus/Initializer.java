package org.jboss.unimbus;

public interface Initializer {
    default void preInitialize() {
    }

    default void postInitialize() {
    }
}
