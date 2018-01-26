package org.jboss.unimbus.config.mp;

import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

class MapConfigSource implements ConfigSource {

    MapConfigSource(String name, Map<String, String> props) {
        this.name = name;
        this.props = props;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.props);
    }

    @Override
    public String getValue(String propertyName) {
        return this.props.get(propertyName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object that) {
        return (this == that);
    }

    private final Map<String, String> props;

    private final String name;
}
