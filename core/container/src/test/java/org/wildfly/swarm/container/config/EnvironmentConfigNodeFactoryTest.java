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
            put("swarm.name", "bob");
            put("swarm.cheese", "cheddar");
            put("not.swarm.taco", "crunchy");
        }};

        ConfigNode node = EnvironmentConfigNodeFactory.load(env);

        assertThat(node.valueOf(ConfigKey.parse("swarm.name"))).isEqualTo("bob");
        assertThat(node.valueOf(ConfigKey.parse("swarm.cheese"))).isEqualTo("cheddar");
        assertThat(node.valueOf(ConfigKey.parse("not.swarm.taco"))).isNull();
    }

    @Test
    public void testLoadNested() {
        Map<String, String> env = new HashMap<String, String>() {{
            put("swarm.http.port", "8080");
            put("swarm.data-sources.ExampleDS.url", "jdbc:db");
            put("SWARM_DATA_DASH_SOURCES_EXAMPLEDS_JNDI_DASH_NAME", "java:/jboss/datasources/example");
            put("SWARM_DATA_UNDERSCORE_SOURCES_EXAMPLEDS_USER_DASH_NAME", "joe");
        }};

        ConfigNode node = EnvironmentConfigNodeFactory.load(env);

        assertThat(node.valueOf(ConfigKey.of("swarm", "http", "port"))).isEqualTo("8080");
        assertThat(node.valueOf(ConfigKey.of("swarm", "data-sources", "ExampleDS", "url"))).isEqualTo("jdbc:db");
        assertThat(node.valueOf(ConfigKey.of("swarm", "data_sources", "ExampleDS", "user-name"))).isEqualTo("joe");
    }
}
