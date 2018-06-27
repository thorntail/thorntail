package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import static io.thorntail.config.impl.sources.ConfigSources.FRAMEWORK_DEFAULTS_ORDINAL;

/**
 * Created by bob on 6/26/18.
 */
public class FrameworkDefaultsConfigSourceProvider implements ConfigSourceProvider {
    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        List<ConfigSource> list = new ArrayList<>();

        try {
            list.addAll(ClasspathConfigSourceLoader.of(forClassLoader,
                                                       FRAMEWORK_DEFAULTS_ORDINAL,
                                                       "META-INF/framework-defaults.properties",
                                                       "META-INF/framework-defaults.yaml",
                                                       "META-INF/framework-defaults.yml"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}
