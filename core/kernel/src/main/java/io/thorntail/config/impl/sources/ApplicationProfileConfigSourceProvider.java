package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.thorntail.config.impl.ConfigLocation;
import io.thorntail.config.impl.Profiles;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import static io.thorntail.config.impl.sources.ConfigSources.APPLICATION_PROFILE_ORDINAL;
import static io.thorntail.config.impl.sources.ConfigSources.LOCATION_ORDINAL;

/**
 * Created by bob on 6/26/18.
 */
public class ApplicationProfileConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        List<ConfigSource> list = new ArrayList<>();

        List<String> profiles = Profiles.getActiveProfiles();

        List<Path> configLocations = ConfigLocation.getConfigLocations();

        for (int i = 0; i < profiles.size(); ++i) {
            String profile = profiles.get(i);
            try {
                list.addAll(ClasspathConfigSourceLoader.of(forClassLoader,
                                                           APPLICATION_PROFILE_ORDINAL + i,
                                                           "META-INF/application-" + profile + ".properties",
                                                           "META-INF/application-" + profile + ".yaml",
                                                           "META-INF/application-" + profile + ".yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Path location : configLocations) {
                if (Files.isDirectory(location)) {
                    try {
                        list.addAll(FilesystemConfigSourceLoader.of(location,
                                                                    APPLICATION_PROFILE_ORDINAL + i,
                                                                    "application-" + profile + ".properties",
                                                                    "application-" + profile + ".yaml",
                                                                    "application-" + profile + ".yml"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }

        for (Path location : configLocations) {
            if (Files.isRegularFile(location)) {
                try {
                    list.add(FilesystemConfigSourceLoader.of(location, LOCATION_ORDINAL));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        return list;
    }

}
