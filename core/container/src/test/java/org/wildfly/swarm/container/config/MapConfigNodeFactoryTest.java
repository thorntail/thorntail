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

        Map<String,Object> input = new HashMap<String,Object>() {{
            put("thorntail", new HashMap<String,Object>() {{
                put("port", 8080);
                put("enabled", true);
                put("things", new ArrayList<Object>() {{
                    add("one");
                    add("two");
                    add(new HashMap<String,Object>() {{
                        put("name", "three");
                        put("cheese", "cheddar");
                        put("item", 3);
                    }});
                }});
            }});
        }};

        ConfigNode config = MapConfigNodeFactory.load(input);

        assertThat(config.valueOf(ConfigKey.parse("thorntail.port"))).isEqualTo("8080");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.enabled"))).isEqualTo("true");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.0"))).isEqualTo("one");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.1"))).isEqualTo("two");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.name"))).isEqualTo("three");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.cheese"))).isEqualTo("cheddar");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.item"))).isEqualTo("3");

    }

    @Test
    public void testLoadBackwardsCompatible() {

        Map<String,Object> input = new HashMap<String,Object>() {{
            put("swarm", new HashMap<String,Object>() {{
                put("port", 8080);
                put("enabled", true);
                put("things", new ArrayList<Object>() {{
                    add("one");
                    add("two");
                    add(new HashMap<String,Object>() {{
                        put("name", "three");
                        put("cheese", "cheddar");
                        put("item", 3);
                    }});
                }});
            }});
        }};

        ConfigNode config = MapConfigNodeFactory.load(input);

        assertThat(config.valueOf(ConfigKey.parse("thorntail.port"))).isEqualTo("8080");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.enabled"))).isEqualTo("true");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.0"))).isEqualTo("one");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.1"))).isEqualTo("two");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.name"))).isEqualTo("three");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.cheese"))).isEqualTo("cheddar");
        assertThat(config.valueOf(ConfigKey.parse("thorntail.things.2.item"))).isEqualTo("3");

    }
}
