package org.wildfly.swarm.container.config;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class EnvironmentConfigNodeFactoryTest {

    @Test
    public void testLoadSimple() {

        Map<String, String> env = new HashMap<String, String>() {{
            put("name", "bob");
            put("cheese", "cheddar");
        }};

        ConfigNode node = EnvironmentConfigNodeFactory.load(env);

        assertThat(node.valueOf(ConfigKey.parse("name"))).isEqualTo("bob");
        assertThat(node.valueOf(ConfigKey.parse("cheese"))).isEqualTo("cheddar");
    }

    @Test
    public void testLoadNested() {
        Map<String, String> env = new HashMap<String, String>() {{
            put("swarm.http.port", "8080");
            put("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigNode node = EnvironmentConfigNodeFactory.load(env);

        assertThat(node.valueOf(ConfigKey.of("swarm", "http", "port"))).isEqualTo("8080");
        assertThat(node.valueOf(ConfigKey.of("swarm", "data-sources", "ExampleDS", "url"))).isEqualTo("jdbc:db");
    }
}
