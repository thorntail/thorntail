package io.thorntail.config.impl;

import java.util.HashMap;
import java.util.Map;

import io.thorntail.config.impl.sources.MapConfigSource;
import org.eclipse.microprofile.config.Config;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

    @Test
    public void testGeneral() {
        Map<String, String> map = new HashMap<>();
        map.put( "server.port", "8080");
        map.put( "jgroups.ports", "9001,9002,9003");

        MapConfigSource source = new MapConfigSource("test", map);
        ConfigBuilderImpl builder = new ConfigBuilderImpl();
        builder.withSources(source);

        Config config = builder.build();
        assertThat(config.getValue( "server.port", Integer.class )).isEqualTo(8080);
        assertThat(config.getValue( "jgroups.ports", String.class )).isEqualTo("9001,9002,9003");
        assertThat(config.getValue( "jgroups.ports", String[].class )).isEqualTo(
                new String[] {
                        "9001",
                        "9002",
                        "9003"
                }
        );
        assertThat(config.getValue( "jgroups.ports", Integer[].class )).isEqualTo(
                new Integer[] {
                        9001,
                        9002,
                        9003
                }
        );

        assertThat(config.getValue( "jgroups.ports", int[].class )).isEqualTo(
                new int[] {
                        9001,
                        9002,
                        9003
                }
        );

    }
}
