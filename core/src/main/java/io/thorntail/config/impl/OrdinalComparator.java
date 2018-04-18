package io.thorntail.config.impl;

import java.util.Comparator;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class OrdinalComparator implements Comparator<ConfigSource> {

    @Override
    public int compare(ConfigSource l, ConfigSource r) {
        int result = -1 * Integer.compare(l.getOrdinal(), r.getOrdinal());
        if (result != 0) {
            return result;
        }

        if (l == r) {
            return 0;
        }

        return -1 * l.getName().compareTo(r.getName());
    }
}
