package org.jboss.unimbus.config.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.jboss.unimbus.config.impl.converters.FallbackConverter;
import org.jboss.unimbus.config.impl.interpolation.Interpolator;

public class ConfigImpl implements Config {

    ConfigImpl(Set<ConfigSource> sources, Map<Class<?>, List<Converter<?>>> converters, List<FallbackConverter> fallbackConverters) {
        this.sources.addAll(sources);
        this.converters.putAll(converters);
        this.fallbackConverters.addAll(fallbackConverters);
        this.interpolator = new Interpolator(this);
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        return getOptionalValue(propertyName, propertyType)
                .orElseThrow(
                        () -> new NoSuchElementException(propertyName)
                );
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        Optional<String> result = this.sources.stream()
                .map(e -> e.getValue(propertyName))
                .filter(Objects::nonNull)
                .findFirst();

        if ( result.isPresent() ) {
            result = Optional.ofNullable( this.interpolator.interpolate(result.get()));
        }

        Optional<T> converted = convert(result, propertyType);

        return converted;
    }

    protected <T> Optional<T> convert(Optional<String> value, Class<T> propertyType) {
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return convert(value.get(), propertyType);
    }

    public <T> Optional<T> convert(String value, Class<T> propertyType) {
        if (propertyType.isArray()) {
            return convertArray(value, propertyType);
        } else {
            Class<?> boxedType = boxedType(propertyType);
            Optional<?> result = convertNonArray(value, boxedType);
            return Optional.ofNullable((T) result.orElse(null));
        }
    }

    protected <T> Optional<T> convertArray(String value, Class<T> arrayType) {
        List<String> components = ArraySplitter.split(value);
        Object converted = Array.newInstance(arrayType.getComponentType(), components.size());

        Class<?> componentType = arrayType.getComponentType();
        Class<?> boxedType = boxedType(componentType);

        for (int i = 0; i < components.size(); ++i) {
            Array.set(converted, i, convertNonArray(components.get(i), boxedType).get());
        }

        return Optional.of((T) converted);
    }



    protected <T> Optional<T> convertNonArray(String value, Class<T> propertyType) {
        if (propertyType.equals(String.class)) {
            return Optional.of(propertyType.cast(value));
        }

        List<Converter<?>> candidates = this.converters.get(propertyType);
        if (candidates != null && !candidates.isEmpty()) {
            Optional<T> result = candidates.stream()
                    .map(e -> e.convert(value))
                    .filter(Objects::nonNull)
                    .map(propertyType::cast)
                    .findFirst();
            if (result.isPresent()) {
                return result;
            }
        }

        Optional<T> result = this.fallbackConverters.stream()
                .map(e -> e.convert(value, propertyType))
                .filter(Objects::nonNull)
                .map(propertyType::cast)
                .findFirst();

        if (result.isPresent()) {
            return result;
        }
        throw new IllegalArgumentException("cannot convert '" + value + "' to " + propertyType);
    }

    Class<?> boxedType(Class<?> type) {
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }

        return type;

    }

    @Override
    public Iterable<String> getPropertyNames() {
        return Collections.unmodifiableCollection(
                this.sources.stream()
                        .map(ConfigSource::getProperties)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (l, r) -> l
                                )
                        )
                        .keySet());
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return Collections.unmodifiableCollection(this.sources);
    }

    private final Map<Class<?>, List<Converter<?>>> converters = new HashMap<>();

    private final Interpolator interpolator;

    private List<FallbackConverter> fallbackConverters = new ArrayList<>();

    private Set<ConfigSource> sources = new TreeSet<>(new OrdinalComparator());


}
