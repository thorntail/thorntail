package io.thorntail.test.impl;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

/**
 * Created by bob on 1/23/18.
 */
public class EphemeralPortsConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        return Collections.singleton(EphemeralPortsConfigSource.INSTANCE);
    }

}
