package org.wildfly.swarm.container.config;

import java.util.Properties;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class PropertiesConfigNodeFactoryTest {

    @Test
    public void testLoadSimple() {

        Properties props = new Properties() {{
            setProperty("name", "bob");
            setProperty("cheese", "cheddar");
        }};

        ConfigNode node = PropertiesConfigNodeFactory.load(props);

        assertThat(node.valueOf(ConfigKey.parse("name"))).isEqualTo("bob");
        assertThat(node.valueOf(ConfigKey.parse("cheese"))).isEqualTo("cheddar");
    }

    @Test
    public void testLoadNested() {
        Properties props = new Properties() {{
            setProperty("swarm.http.port", "8080");
            setProperty("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigNode node = PropertiesConfigNodeFactory.load(props);

        assertThat(node.valueOf(ConfigKey.of("swarm", "http", "port"))).isEqualTo("8080");
        assertThat(node.valueOf(ConfigKey.of("swarm", "data-sources", "ExampleDS", "url"))).isEqualTo("jdbc:db");
    }
}
