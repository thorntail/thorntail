package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class FilesystemConfigSourceLoader {

    public static List<ConfigSource> of(Path dir, int defaultOrdinal, String... paths) throws IOException {
        List<ConfigSource> sources = new ArrayList<>();

        for (String path : paths) {
            Path file = dir.resolve(path);
            if (!Files.exists(file)) {
                continue;
            }
            ConfigSource source = of(file, defaultOrdinal);
            if (source != null) {
                sources.add(source);
            }
        }

        return sources;
    }

    public static ConfigSource of(Path file, int defaultOrdinal) throws IOException {
        String name = file.getFileName().toString();

        if (name.endsWith(".properties")) {
            return PropertiesConfigSource.of(file.toUri().toURL(), defaultOrdinal);
        } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return YamlConfigSource.of(file.toUri().toURL(), defaultOrdinal);
        }

        return null;
    }

}
