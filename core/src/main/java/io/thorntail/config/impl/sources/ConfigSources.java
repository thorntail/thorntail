package io.thorntail.config.impl.sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.thorntail.config.impl.ConfigLocation;
import io.thorntail.config.impl.Profiles;
import io.thorntail.config.impl.sources.spec.SystemEnvironmentConfigSource;
import io.thorntail.config.impl.sources.spec.SystemPropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Created by bob on 2/7/18.
 */
public class ConfigSources {

    public static int FRAMEWORK_DEFAULTS_ORDINAL = -1000;

    public static int MICROPROFILE_CONFIG_ORDINAL = 100;

    public static int APPLICATION_ORDINAL = 200;

    public static int APPLICATION_PROFILE_ORDINAL = 250;

    public static int LOCATION_ORDINAL = 275;

    public static int ENVIRONMENT_VARIABLES_ORDINAL = 300;

    public static int SYSTEM_PROPERTIES_ORDINAL = 400;


    public static ConfigSource systemProperties() {
        return new SystemPropertiesConfigSource();
    }

    public static ConfigSource systemEnvironment() {
        return new SystemEnvironmentConfigSource();
    }

    public static List<ConfigSource> frameworkDefaults(ClassLoader classLoader) {
        List<ConfigSource> list = new ArrayList<>();

        try {
            list.addAll(ClasspathConfigSourceLoader.of(classLoader,
                                                       FRAMEWORK_DEFAULTS_ORDINAL,
                                                       "META-INF/framework-defaults.properties",
                                                       "META-INF/framework-defaults.yaml",
                                                       "META-INF/framework-defaults.yml"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public static List<ConfigSource> application(ClassLoader classLoader) {
        List<ConfigSource> list = new ArrayList<>();

        try {
            list.addAll(ClasspathConfigSourceLoader.of(classLoader,
                                                       APPLICATION_ORDINAL,
                                                       "META-INF/application.properties",
                                                       "META-INF/application.yaml",
                                                       "META-INF/application.yml"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public static List<ConfigSource> applicationProfiles(ClassLoader classLoader) {
        List<ConfigSource> list = new ArrayList<>();

        List<String> profiles = Profiles.getActiveProfiles();

        List<Path> configLocations = ConfigLocation.getConfigLocations();

        for (int i = 0; i < profiles.size(); ++i) {
            String profile = profiles.get(i);
            try {
                list.addAll(ClasspathConfigSourceLoader.of(classLoader,
                                                           APPLICATION_PROFILE_ORDINAL + i,
                                                           "META-INF/application-" + profile + ".properties",
                                                           "META-INF/application-" + profile + ".yaml",
                                                           "META-INF/application-" + profile + ".yml"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Path location : configLocations) {
                if (Files.isDirectory(location)) {
                    try {
                        list.addAll(FilesystemConfigSourceLoader.of(location,
                                                                    APPLICATION_PROFILE_ORDINAL + i,
                                                                    "application-" + profile + ".properties",
                                                                    "application-" + profile + ".yaml",
                                                                    "application-" + profile + ".yml"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }

        for (Path location : configLocations) {
            if (Files.isRegularFile(location)) {
                try {
                    list.add(FilesystemConfigSourceLoader.of(location, LOCATION_ORDINAL));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        return list;
    }

    public static List<ConfigSource> microprofileConfig(ClassLoader classLoader) {
        List<ConfigSource> list = new ArrayList<>();

        try {

            list.addAll(ClasspathConfigSourceLoader.of(classLoader,
                                                       MICROPROFILE_CONFIG_ORDINAL,
                                                       "META-INF/microprofile-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }


}
