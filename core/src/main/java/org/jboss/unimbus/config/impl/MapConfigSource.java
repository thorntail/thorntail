package org.jboss.unimbus.config.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class MapConfigSource implements ConfigSource {

    public MapConfigSource(String name) {
        this( name, new HashMap<>());
    }

    public MapConfigSource(String name, Map<String, String> props) {
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

    public void setValue(String propertyName, String value) {
        this.props.put(propertyName, value);
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
