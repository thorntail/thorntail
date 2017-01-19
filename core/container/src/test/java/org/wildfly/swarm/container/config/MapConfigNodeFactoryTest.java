package org.wildfly.swarm.container.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class MapConfigNodeFactoryTest {

    @Test
    public void testLoad() {

        Map input = new HashMap() {{
            put("swarm", new HashMap() {{
                put("port", 8080);
                put("enabled", true);
                put("things", new ArrayList() {{
                    add("one");
                    add("two");
                    add(new HashMap() {{
                        put("name", "three");
                        put("cheese", "cheddar");
                        put("item", 3);
                    }});
                }});
            }});
        }};

        ConfigNode config = MapConfigNodeFactory.load(input);

        assertThat(config.valueOf(ConfigKey.parse("swarm.port"))).isEqualTo("8080");
        assertThat(config.valueOf(ConfigKey.parse("swarm.enabled"))).isEqualTo("true");
        assertThat(config.valueOf(ConfigKey.parse("swarm.things.0"))).isEqualTo("one");
        assertThat(config.valueOf(ConfigKey.parse("swarm.things.1"))).isEqualTo("two");
        assertThat(config.valueOf(ConfigKey.parse("swarm.things.2.name"))).isEqualTo("three");
        assertThat(config.valueOf(ConfigKey.parse("swarm.things.2.cheese"))).isEqualTo("cheddar");
        assertThat(config.valueOf(ConfigKey.parse("swarm.things.2.item"))).isEqualTo("3");

    }
}
