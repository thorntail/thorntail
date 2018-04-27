package io.thorntail.config.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.thorntail.Info.KEY;

/**
 * Created by bob on 2/6/18.
 */
public class ConfigLocation {

    public static final String CONFIG_LOCATION_PROPERTY_NAME = KEY + ".config.location";

    public static final String CONFIG_LOCATION_ENVIRONMENT_VARIABLE_NAME = KEY.toUpperCase() + "_CONFIG_LOCATION";

    public static List<Path> getConfigLocations() {
        String value = System.getProperty(CONFIG_LOCATION_PROPERTY_NAME);
        if (value == null) {
            value = System.getenv(CONFIG_LOCATION_ENVIRONMENT_VARIABLE_NAME);
        }

        if (value == null) {
            return Collections.emptyList();
        }

        String[] locations = value.split(File.pathSeparator);

        return Arrays.stream(locations)
                .map(e-> Paths.get(e))
                .collect(Collectors.toList());
    }

}
