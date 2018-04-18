package io.thorntail.util;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilities for working with Java classes and types.
 */
public class Types {

    private Types() {

    }

    /** Deteremine the full type closure of a given class.
     *
     * @param cls The class to inspect.
     * @return A set of all classes and interfaces.
     */
    public static Set<Type> getTypeClosure(Class<?> cls) {
        Set<Type> types = new HashSet<>();
        getTypeClosure(types, cls);
        return types;
    }

    public static Set<Type> getInterfaces(Class<?> cls) {
        return filterInterfaces(getTypeClosure(cls));
    }

    public static Set<Type> filterInterfaces(Set<Type> types) {
        return types.stream()
                .filter(e->e instanceof Class)
                .filter(e->((Class)e).isInterface())
                .collect(Collectors.toSet());
    }

    private static void getTypeClosure(Set<Type> set, Class<?> cls) {
        if ( cls == null ) {
            return;
        }
        if ( set.contains(cls)) {
            return;
        }

        set.add( cls );
        for (Class<?> iface : cls.getInterfaces()) {
            getTypeClosure(set, iface);
        }

        getTypeClosure(set, cls.getSuperclass());
    }


}
