package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.thorntail.config.impl.ConfigLocation;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import static io.thorntail.config.impl.sources.ConfigSources.APPLICATION_ORDINAL;

/**
 * Created by bob on 6/26/18.
 */
public class ApplicationConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        List<ConfigSource> list = new ArrayList<>();

        List<Path> configLocations = ConfigLocation.getConfigLocations();

        for (Path location : configLocations) {
            if (Files.isDirectory(location)) {
                try {
                    list.addAll(FilesystemConfigSourceLoader.of(location,
                                                                APPLICATION_ORDINAL,
                                                                "application.properties",
                                                                "application.yaml",
                                                                "application.yml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            list.addAll(ClasspathConfigSourceLoader.of(forClassLoader,
                                                       APPLICATION_ORDINAL,
                                                       "META-INF/application.properties",
                                                       "META-INF/application.yaml",
                                                       "META-INF/application.yml"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

}
