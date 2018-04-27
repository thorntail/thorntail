package io.thorntail.config.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.thorntail.Info.KEY;

/**
 * Created by bob on 2/6/18.
 */
public class Profiles {

    public static final String PROFILES_PROPERTY_NAME = KEY + ".profiles";

    public static final String PROFILES_ENVIRONMENT_VARIABLE_NAME = KEY.toUpperCase() + "_PROFILES";

    public static List<String> getActiveProfiles() {
        String value = System.getProperty(PROFILES_PROPERTY_NAME);
        if (value == null) {
            value = System.getenv(PROFILES_ENVIRONMENT_VARIABLE_NAME);
        }

        if (value == null) {
            return Collections.emptyList();
        }

        String[] profiles = value.split(",");

        return Arrays.asList(profiles);
    }

}
