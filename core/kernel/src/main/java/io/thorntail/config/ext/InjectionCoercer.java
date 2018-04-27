package io.thorntail.config.ext;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.inject.Provider;

import io.thorntail.config.impl.ConfigImpl;

/**
 * Created by bob on 2/2/18.
 */
class InjectionCoercer {

    private static Map<Type, InjectionCoercer> CACHE = new HashMap<>();

    public static InjectionCoercer of(Type targetType) {
        synchronized (CACHE) {
            InjectionCoercer entry = CACHE.get(targetType);
            if (entry == null) {
                entry = new InjectionCoercer(targetType);
                CACHE.put(targetType, entry);
            }
            return entry;
        }
    }

    InjectionCoercer(Type targetType) {
        this.targetType = targetType;
        initialize();
    }

    void initialize() {
        if (this.targetType instanceof Class<?>) {
            this.requestType = (Class<?>) this.targetType;
        } else if (this.targetType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) this.targetType).getRawType();
            if (rawType instanceof Class<?>) {
                if (Collection.class.isAssignableFrom((Class<?>) rawType)) {
                    this.collectionType = (Class<? extends Collection>) rawType;
                    Class<?> componentType = (Class<?>) ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    Object array = Array.newInstance(componentType, 1);
                    this.requestType = array.getClass();
                } else if (Optional.class == rawType) {
                    Type innerType = ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    if (innerType instanceof ParameterizedType) {
                        Type innerRawType = ((ParameterizedType) innerType).getRawType();
                        if (Collection.class.isAssignableFrom((Class<?>) innerRawType)) {
                            this.collectionType = (Class<? extends Collection>) innerRawType;
                            Class<?> componentType = (Class<?>) ((ParameterizedType) innerType).getActualTypeArguments()[0];
                            Object array = Array.newInstance(componentType, 1);
                            this.requestType = array.getClass();
                        }
                    } else {
                        this.requestType = (Class<?>) ((ParameterizedType) this.targetType).getActualTypeArguments()[0];
                    }
                    this.optional = true;
                } else if (Provider.class == rawType) {
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

        return doCoerce(config, name, defaultValue);
    }

    Object doCoerce(ConfigImpl config, String name, String defaultValue) throws IllegalAccessException, InstantiationException {

        Optional<?> result = config.getOptionalValue(name, this.requestType);
        if (result.isPresent()) {
            return coerce(result.get());
        }

        if (defaultValue == null && this.optional) {
            return Optional.empty();
        }

        if (defaultValue == null) {
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
            if (this.dynamic) {
                return input;
            }

            if (this.optional) {
                if (this.collectionType != null) {
                    return Optional.ofNullable(toCollection(this.collectionType, input));
                } else {
                    return Optional.ofNullable(input);
                }
            }
            if (this.collectionType != null) {
                return toCollection(this.collectionType, input);
            }
        }

        return null;
    }

    Collection<?> toCollection(Class<? extends Collection> collectionType, Object array) throws IllegalAccessException, InstantiationException {
        if (array == null) {
            return null;
        }
        Collection collection = concreteTypeOf(collectionType).newInstance();

        int len = Array.getLength(array);

        for (int i = 0; i < len; ++i) {
            collection.add(Array.get(array, i));
        }

        return collection;
    }

    Class<? extends Collection> concreteTypeOf(Class<? extends Collection> collectionType) {
        if (!collectionType.isInterface() && !Modifier.isAbstract(collectionType.getModifiers())) {
            return collectionType;
        }

        if (collectionType == List.class) {
            return ArrayList.class;
        }

        if (collectionType == Set.class) {
            return HashSet.class;
        }

        throw new IllegalArgumentException("Unsupported collection type: " + collectionType.getName());

    }


    private Class<? extends Collection> collectionType;

    private final Type targetType;

    private Class<?> requestType;

    private boolean optional;

    private boolean dynamic;
}
