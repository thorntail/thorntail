package io.thorntail.config.impl;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.smallrye.config.SmallRyeConfig;
import io.thorntail.config.impl.interpolation.Interpolator;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * Created by bob on 6/26/18.
 */
public class InterpolatingConfig extends SmallRyeConfig implements Serializable {
    InterpolatingConfig(List<ConfigSource> configSources, Map<Type, Converter> converters) {
        super(configSources, converters);
        this.interpolator = new Interpolator(this);
    }

    @Override
    protected <T> Converter getConverter(Class<T> asType) {
        Converter delegate = super.getConverter(asType);
        return new InterpolatingConverter(this.interpolator, delegate);
    }

    private final Interpolator interpolator;
}
