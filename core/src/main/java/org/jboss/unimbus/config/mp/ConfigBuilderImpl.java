package org.jboss.unimbus.config.mp;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;
import org.jboss.unimbus.config.mp.converters.BooleanConverter;
import org.jboss.unimbus.config.mp.converters.ClassConverter;
import org.jboss.unimbus.config.mp.converters.DoubleConverter;
import org.jboss.unimbus.config.mp.converters.FallbackConverter;
import org.jboss.unimbus.config.mp.converters.FloatConverter;
import org.jboss.unimbus.config.mp.converters.IntegerConverter;
import org.jboss.unimbus.config.mp.converters.LongConverter;
import org.jboss.unimbus.config.mp.converters.fallback.EnumValueOfConverter;
import org.jboss.unimbus.config.mp.converters.fallback.StaticParseConverter;
import org.jboss.unimbus.config.mp.converters.fallback.StaticValueOfConverter;
import org.jboss.unimbus.config.mp.converters.fallback.StringConstructorConverter;
import org.jboss.unimbus.config.mp.converters.URLConverter;

class ConfigBuilderImpl implements ConfigBuilder {

    @Override
    public ConfigBuilder addDefaultSources() {
        this.addDefaultSources = true;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        this.addDiscoveredSources = true;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        this.addDiscoveredConverters = true;
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        if (loader != null) {
            this.classLoader = loader;
        }
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        for (ConfigSource source : sources) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        for (Converter<?> converter : converters) {
            this.converters.add(new ConverterHolder(null, 100, converter));
        }
        return this;
    }

    @Override
    public <T> ConfigBuilder withConverter(Class<T> type, int priority, Converter<T> converter) {
        this.converters.add(new ConverterHolder<T>(type, priority, converter));
        return this;
    }

    @Override
    public Config build() {
        if (this.addDefaultSources) {
            this.sources.add(new SystemPropertiesConfigSource());
            this.sources.add(new SystemEnvironmentConfigSource());
            this.sources.add(new MetaInfPropertiesConfigSource(this.classLoader));
            this.sources.add(new UNimbusDefaultsConfigSource(this.classLoader));
        }

        if (this.addDiscoveredSources) {
            ServiceLoader<ConfigSource> discovered = ServiceLoader.load(ConfigSource.class);
            for (ConfigSource each : discovered) {
                this.sources.add(each);
            }
        }

        Map<Class<?>, Set<ConverterHolder<?>>> configConverters = new HashMap<>();

        withConverter(Boolean.class, 1, new BooleanConverter());
        withConverter(Integer.class, 1, new IntegerConverter());
        withConverter(Long.class, 1, new LongConverter());
        withConverter(Double.class, 1, new DoubleConverter());
        withConverter(Float.class, 1, new FloatConverter());
        withConverter(Class.class, 1, new ClassConverter(this.classLoader));
        withConverter(URL.class, 1, new URLConverter());

        for (ConverterHolder each : this.converters) {
            Class<?> type = typeOf(each.converter);
            Set<ConverterHolder<?>> set = configConverters.get(type);
            if (set == null) {
                set = new TreeSet<>();
                configConverters.put(type, set);
            }
            set.add(each);
        }

        if (this.addDiscoveredConverters) {
            ServiceLoader<Converter> discovered = ServiceLoader.load(Converter.class);
            for (Converter each : discovered) {
                Class<?> type = typeOf(each);
                Set<ConverterHolder<?>> set = configConverters.get(type);
                if (set == null) {
                    set = new TreeSet<>();
                    configConverters.put(type, set);
                }
                set.add(new ConverterHolder<>(type, 100, each));
            }
        }

        Map<Class<?>, List<Converter<?>>> convertersMap = configConverters.entrySet().stream()
                .collect(Collectors.toMap(
                        k -> k.getKey(),
                        v -> v.getValue().stream().map(e -> e.converter).collect(Collectors.toList())
                ));

        List<FallbackConverter> fallbackConverters = new ArrayList<>();

        fallbackConverters.add(new StringConstructorConverter());
        fallbackConverters.add(new StaticValueOfConverter());
        fallbackConverters.add(new StaticParseConverter());
        fallbackConverters.add(new EnumValueOfConverter());

        ConfigImpl config = new ConfigImpl(this.sources, convertersMap, fallbackConverters);
        return config;
    }

    private Class<?> typeOf(Converter converter) {
        try {
            Method method = converter.getClass().getMethod("convert", String.class);
            return method.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<ConfigSource> sources = new TreeSet<>(new OrdinalComparator());

    private Set<ConverterHolder> converters = new HashSet<>();

    private boolean addDefaultSources = false;

    private boolean addDiscoveredSources = false;

    private boolean addDiscoveredConverters = false;

    private ClassLoader classLoader = ConfigBuilderImpl.class.getClassLoader();

    private static class ConverterHolder<T> implements Comparable<ConverterHolder<T>> {
        ConverterHolder(Class<T> type, int priority, Converter<T> converter) {
            this.type = type;
            this.priority = priority;
            this.converter = converter;
        }

        @Override
        public int compareTo(ConverterHolder<T> that) {
            return -1 * Integer.compare(this.priority, that.priority);
        }

        final Class<T> type;

        final int priority;

        final Converter<T> converter;
    }
}
