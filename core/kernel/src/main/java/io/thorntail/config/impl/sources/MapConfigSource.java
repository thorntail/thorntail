package io.thorntail.config.impl.sources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class MapConfigSource implements ConfigSource {

    public MapConfigSource(String name) {
        this(name, new HashMap<>(), 100);
    }

    public MapConfigSource(String name, int defaultOrdinal) {
        this(name, new HashMap<>(), defaultOrdinal);
    }

    public MapConfigSource(String name, Map<String, String> props) {
        this(name, props, 100);
    }

    public MapConfigSource(String name, Map<String, String> props, int defaultOrdinal) {
        this.name = name;
        this.props = props;
        this.defaultOrdinal = defaultOrdinal;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.props);
    }

    @Override
    public String getValue(String propertyName) {
        return this.props.get(propertyName);
    }

    public void setValue(String propertyName, String value) {
        this.props.put(propertyName, value);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getOrdinal() {
        String configOrdinal = getValue(CONFIG_ORDINAL);
        if (configOrdinal != null) {
            try {
                return Integer.parseInt(configOrdinal);
            } catch (NumberFormatException ignored) {

            }
        }
        return this.defaultOrdinal;
    }

    @Override
    public boolean equals(Object that) {
        return (this == that);
    }

    private final Map<String, String> props;

    private final String name;

    private int defaultOrdinal;
}
