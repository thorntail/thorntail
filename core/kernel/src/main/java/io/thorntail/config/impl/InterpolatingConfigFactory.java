package io.thorntail.config.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.smallrye.config.ConfigFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

/**
 * Created by bob on 6/26/18.
 */
public class InterpolatingConfigFactory implements ConfigFactory {

    @Override
    public Config newConfig(List<ConfigSource> sources, Map<Type, Converter> configConverters) {
        return new InterpolatingConfig( sources, configConverters );
    }
}
