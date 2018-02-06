package org.jboss.unimbus.config.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

import static org.jboss.unimbus.UNimbus.PROJECT_KEY;

/**
 * Created by bob on 2/6/18.
 */
public class Profiles {

    public static final String PROFILES_PROPERTY_NAME = PROJECT_KEY + ".profiles";

    public static final String PROFILES_ENVIRONMENT_VARIABLE_NAME = PROJECT_KEY.toUpperCase() + "_PROFILES";

    static List<ConfigSource> getConfigSources(ClassLoader classLoader) {
        String value = System.getProperty(PROFILES_ENVIRONMENT_VARIABLE_NAME);
        if (value == null) {
            value = System.getenv(PROFILES_ENVIRONMENT_VARIABLE_NAME);
        }

        if (value == null) {
            return Collections.emptyList();
        }

        List<ConfigSource> sources = new ArrayList<>();

        String[] profiles = value.split(",");

        for (String profile : profiles) {
            sources.addAll(new ApplicationProfilePropertiesConfigSourceProvider(profile).getConfigSources(classLoader));
            sources.addAll(new ApplicationProfileYamlConfigSourceProvider(profile).getConfigSources(classLoader));
        }

        return sources;
    }


}
