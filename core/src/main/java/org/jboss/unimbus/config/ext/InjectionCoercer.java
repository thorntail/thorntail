package org.jboss.unimbus.config.ext;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.inject.Provider;

import org.jboss.unimbus.config.impl.ConfigImpl;

/**
 * Created by bob on 2/2/18.
 */
class InjectionCoercer {

    InjectionCoercer(Type targetType) {
        this.targetType = targetType;
        initialize();
    }

    void initialize() {
        if ( this.targetType instanceof Class<?>) {
            this.requestType = (Class<?>) this.targetType;
        } else if ( this.targetType instanceof ParameterizedType ) {
            Type rawType = ((ParameterizedType) this.targetType).getRawType();
            if ( rawType instanceof Class<?> ) {
                if ( Collection.class.isAssignableFrom((Class<?>) rawType)) {
                    Class<?> componentType = (Class<?>) ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    Object array = Array.newInstance(componentType,1);
                    this.requestType = array.getClass();
                } else if ( Optional.class == rawType ) {
                    this.requestType = (Class<?>) ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    this.optional = true;
                } else if (Provider.class == rawType ) {
                    this.requestType = (Class<?>) ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    this.optional = true;
                    this.dynamic = true;
                }
            }
        }
    }

    Type getTargetType() {
        return this.targetType;
    }

    Class<?> getRequestType() {
        return this.requestType;
    }

    boolean isOptional() {
        return this.optional;
    }

    boolean isDynamic() {
        return this.dynamic;
    }

    Object coerce(ConfigImpl config, String name, String defaultValue) throws IllegalAccessException, InstantiationException {
        if (this.dynamic) {
            return providerOf(config, name, defaultValue);
        }

        return doCoerce(config,name, defaultValue);
    }

    Object doCoerce(ConfigImpl config, String name, String defaultValue) throws IllegalAccessException, InstantiationException {

        Optional<?> result = config.getOptionalValue(name, this.requestType);
        if (result.isPresent()) {
            return coerce(result.get());
        }

        if ( defaultValue == null && this.optional ) {
            return Optional.empty();
        }

        if ( defaultValue == null ) {
            throw new NoSuchElementException(name);
        }

        return coerce(config.convert(defaultValue, getRequestType()).get());
    }

    private Provider<?> providerOf(ConfigImpl config, String name, String defaultValue) {
        return (Provider<Object>) () -> {
            try {
                return doCoerce(config, name, defaultValue);
            } catch (IllegalAccessException | InstantiationException e) {
                // ignore?
            }
            return null;
        };
    }

    Object coerce(Object input) throws InstantiationException, IllegalAccessException {
        if (this.targetType instanceof Class) {
            if (((Class) this.targetType).isInstance(input)) {
                return input;
            }
        } else if (this.targetType instanceof ParameterizedType) {
            if ( this.dynamic ) {
                return input;
            }
            if ( this.optional ) {
                return Optional.ofNullable(input);
            }
            Type rawType = ((ParameterizedType) this.targetType).getRawType();
            if ( Collection.class.isAssignableFrom((Class<?>) rawType)) {
                return toCollection((Class<? extends Collection>) rawType, input);
            }
        }

        return null;
    }

    Collection<?> toCollection(Class<? extends Collection> collectionType, Object array) throws IllegalAccessException, InstantiationException {
        Collection collection = concreteTypeOf(collectionType).newInstance();

        int len = Array.getLength(array);

        for ( int i = 0 ; i < len ; ++i ) {
            collection.add( Array.get(array, i));
        }

        return collection;
    }

    Class<? extends Collection> concreteTypeOf(Class<? extends Collection> collectionType) {
        if ( ! collectionType.isInterface() && !Modifier.isAbstract(collectionType.getModifiers())) {
            return collectionType;
        }

        if ( collectionType == List.class ) {
            return ArrayList.class;
        }

        if ( collectionType == Set.class ) {
            return HashSet.class;
        }

        throw new IllegalArgumentException("Unsupported collection type: " + collectionType.getName());

    }


    private final Type targetType;
    private Class<?> requestType;
    private boolean optional;
    private boolean dynamic;
}
