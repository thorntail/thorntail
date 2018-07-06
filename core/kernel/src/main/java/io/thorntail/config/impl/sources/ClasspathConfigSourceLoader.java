package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

class ClasspathConfigSourceLoader {

    static List<ConfigSource> of(ClassLoader classLoader, int defaultOrdinal, String... paths) throws IOException {
        Set<URL> seenUrls = new HashSet<>();
        List<ConfigSource> sources = new ArrayList<>();

        for (String path : paths) {
            sources.addAll(process(classLoader, defaultOrdinal, path, seenUrls));
            if (Thread.currentThread().getContextClassLoader() != classLoader) {
                sources.addAll(process(Thread.currentThread().getContextClassLoader(), defaultOrdinal, path, seenUrls));
            }
        }


        return sources;
    }

    static List<ConfigSource> process(ClassLoader classLoader, int defaultOrdinal, String path, Set<URL> seenUrls) throws IOException {
        List<ConfigSource> sources = new ArrayList<>();
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL each = resources.nextElement();
            if (seenUrls.contains(each)) {
                continue;
            }
            seenUrls.add(each);
            ConfigSource source = process(defaultOrdinal, each);
            if (source != null) {
                sources.add(source);
            }
        }
        return sources;
    }

    static ConfigSource process(int defaultOrdinal, URL url) throws IOException {
        if (url.getPath().endsWith(".properties")) {
            return PropertiesConfigSource.of(url, defaultOrdinal);
        } else if (url.getPath().endsWith(".yaml") || url.getPath().endsWith(".yml")) {
            return YamlConfigSource.of(url, defaultOrdinal);
        }
        return null;
    }

}
