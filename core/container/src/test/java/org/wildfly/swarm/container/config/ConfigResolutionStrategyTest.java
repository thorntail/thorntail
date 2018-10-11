/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            setProperty("thorntail.http.port", "8080");
            setProperty("thorntail.data-sources.ExampleDS.url", "jdbc:db");
        }};
        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
    }

    @Test
    public void testOnlyPropertiesBackwardsCompatible() {
        Properties props = new Properties() {{
            setProperty("swarm.http.port", "8080");
            setProperty("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};
        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
    }

    @Test
    public void testOnlyConfig() {
        Properties props = new Properties();

        ConfigNode config = new ConfigNode() {{
            recursiveChild("thorntail.http.port", "8080");
            recursiveChild("thorntail.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
    }

    @Test
    public void testOnlyConfigBackwardsCompatible() {
        Properties props = new Properties();

        ConfigNode config = new ConfigNode() {{
            recursiveChild("swarm.http.port", "8080");
            recursiveChild("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
    }

    @Test
    public void testNonOverlappingMerge() {
        Properties props = new Properties() {{
            setProperty("thorntail.https.port", "8443");
            setProperty("thorntail.data-sources.ExampleDS.driver-name", "cooper");
        }};

        ConfigNode config = new ConfigNode() {{
            recursiveChild("thorntail.http.port", "8080");
            recursiveChild("thorntail.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }

    @Test
    public void testNonOverlappingMergeBackwardsCompatible() {
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

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:db");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:db");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }

    @Test
    public void testOverlappingMerge() {
        Properties props = new Properties() {{
            setProperty("thorntail.https.port", "8443");
            setProperty("thorntail.data-sources.ExampleDS.driver-name", "cooper");
            setProperty("thorntail.data-sources.ExampleDS.url", "jdbc:otherwise");
        }};

        ConfigNode config = new ConfigNode() {{
            recursiveChild("thorntail.http.port", "8080");
            recursiveChild("thorntail.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigResolutionStrategy strategy = new ConfigResolutionStrategy(props);
        strategy.add(config);
        strategy.activate();

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:otherwise");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:otherwise");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }

    @Test
    public void testOverlappingMergeBackwardsCompatible() {
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

        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.http.port"))).isEqualTo("8080");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.https.port"))).isEqualTo("8443");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.url"))).isEqualTo("jdbc:otherwise");
        assertThat(strategy.valueOf(ConfigKey.parse("thorntail.data-sources.ExampleDS.driver-name"))).isEqualTo("cooper");

        assertThat(props.getProperty("thorntail.http.port")).isEqualTo("8080");
        assertThat(props.getProperty("thorntail.https.port")).isEqualTo("8443");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.url")).isEqualTo("jdbc:otherwise");
        assertThat(props.getProperty("thorntail.data-sources.ExampleDS.driver-name")).isEqualTo("cooper");
    }


}
