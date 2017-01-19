package org.wildfly.swarm.container.config;

import java.util.Properties;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigResolutionStrategyTest {

    @Test
    public void testCompletelyEmpty() {
        Properties props = new Properties();
        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.activate();
    }

    @Test
    public void testOnlyProperties() {
        Properties props = new Properties() {{
            setProperty("swarm.http.port", "8080");
            setProperty("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};
        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("swarm.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
    }

    @Test
    public void testOnlyConfig() {
        Properties props = new Properties();

        ConfigNode config = new ConfigNode() {{
            recursiveChild("swarm.http.port", "8080");
            recursiveChild("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("swarm.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");

        assertThat(props.getProperty("swarm.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("swarm.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
    }

    @Test
    public void testNonOverlappingMerge() {
        Properties props = new Properties() {{
            setProperty("swarm.https.port", "8443");
            setProperty("swarm.data-sources.ExampleDS.driver-name", "cooper");
        }};

        ConfigNode config = new ConfigNode() {{
            recursiveChild("swarm.http.port", "8080");
            recursiveChild("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("swarm.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("swarm.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("swarm.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("swarm.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
        assertThat(props.getProperty("swarm.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }

    @Test
    public void testOverlappingMerge() {
        Properties props = new Properties() {{
            setProperty("swarm.https.port", "8443");
            setProperty("swarm.data-sources.ExampleDS.driver-name", "cooper");
            setProperty("swarm.data-sources.ExampleDS.url", "jdbc:otherwise");
        }};

        ConfigNode config = new ConfigNode() {{
            recursiveChild("swarm.http.port", "8080");
            recursiveChild("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("swarm.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.url"))).isEqualTo("jdbc:otherwise");
        assertThat(strategy.valueOf(ConfigKey.parse("swarm.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("swarm.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("swarm.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("swarm.data-sources.ExampleDS.url")).isEqualTo("jdbc:otherwise");
        assertThat(props.getProperty("swarm.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }


}
