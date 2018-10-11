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
            setProperty("thorntail.http.port", "8080");
            setProperty("thorntail.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigNode node = PropertiesConfigNodeFactory.load(props);

        assertThat(node.valueOf(ConfigKey.of("thorntail", "http", "port"))).isEqualTo("8080");
        assertThat(node.valueOf(ConfigKey.of("thorntail", "data-sources", "ExampleDS", "url"))).isEqualTo("jdbc:db");
    }

    @Test
    public void testLoadNestedBackwardsCompatible() {
        Properties props = new Properties() {{
            setProperty("swarm.http.port", "8080");
            setProperty("swarm.data-sources.ExampleDS.url", "jdbc:db");
        }};

        ConfigNode node = PropertiesConfigNodeFactory.load(props);

        assertThat(node.valueOf(ConfigKey.of("thorntail", "http", "port"))).isEqualTo("8080");
        assertThat(node.valueOf(ConfigKey.of("thorntail", "data-sources", "ExampleDS", "url"))).isEqualTo("jdbc:db");
    }
}
