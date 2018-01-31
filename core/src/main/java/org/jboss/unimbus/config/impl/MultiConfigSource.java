package org.jboss.unimbus.config.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.spi.ConfigSource;

class MultiConfigSource implements ConfigSource {

    MultiConfigSource() {

    }

    void addConfigSource(ConfigSource source) {
        this.sources.add(source);
    }

    @Override
    public Map<String, String> getProperties() {
        return this.sources.stream()
                .map(ConfigSource::getProperties)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (l, r) -> l
                        )
                );
    }

    @Override
    public String getValue(String propertyName) {
        return this.sources.stream()
                .map(e -> e.getValue(propertyName))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getName() {
        return "multi: " + this.sources.stream().map(e -> e.getName()).collect(Collectors.joining(","));
    }

    private Set<ConfigSource> sources = new TreeSet<>(new OrdinalComparator());
}
