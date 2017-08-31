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
public class ConfigViewTest {

    @Test
    public void testDefaultsOnly() {

        ConfigNode defaults = new ConfigNode() {{
            child("cheese", "cheddar");
            child("amount", "42");
            child("company", new ConfigNode() {{
                child("name", "cheeseCorp");
                child("founded", "2017");
                child("owners", new ConfigNode() {{
                    child("0", "bob");
                    child("1", "melissa");
                }});
            }});
        }};

        Properties props = new Properties();

        ConfigViewImpl view = new ConfigViewImpl();
        view.withDefaults(defaults);
        view.withProperties(props);
        view.activate();

        assertThat(props.getProperty("cheese")).isEqualTo("cheddar");
        assertThat(props.getProperty("amount")).isEqualTo("42");
        assertThat(props.getProperty("company.name")).isEqualTo("cheeseCorp");
        assertThat(props.getProperty("company.founded")).isEqualTo("2017");
        assertThat(props.getProperty("company.owners.0")).isEqualTo("bob");
        assertThat(props.getProperty("company.owners.1")).isEqualTo("melissa");

    }

    @Test
    public void testDefaultsWithPropertyOverride() {
        ConfigNode defaults = new ConfigNode() {{
            child("cheese", "cheddar");
            child("amount", "42");
            child("company", new ConfigNode() {{
                child("name", "cheeseCorp");
                child("founded", "2017");
                child("owners", new ConfigNode() {{
                    child("0", "bob");
                    child("1", "melissa");
                }});
            }});
        }};

        Properties props = new Properties() {{
            setProperty("company.name", "Wheel O'Cheese");
        }};


        ConfigViewImpl view = new ConfigViewImpl();
        view.withDefaults(defaults);
        view.withProperties(props);
        view.activate();

        assertThat(props.getProperty("cheese")).isEqualTo("cheddar");
        assertThat(props.getProperty("amount")).isEqualTo("42");
        assertThat(props.getProperty("company.name")).isEqualTo("Wheel O'Cheese");
        assertThat(props.getProperty("company.founded")).isEqualTo("2017");
        assertThat(props.getProperty("company.owners.0")).isEqualTo("bob");
        assertThat(props.getProperty("company.owners.1")).isEqualTo("melissa");
    }

    @Test
    public void testExplicitActivationOfOne() {

        ConfigNode defaults = new ConfigNode() {{
            child("cheese", "cheddar");
            child("amount", "42");
            child("company", new ConfigNode() {{
                child("name", "cheeseCorp");
                child("founded", "2017");
                child("owners", new ConfigNode() {{
                    child("0", "bob");
                    child("1", "melissa");
                }});
            }});
        }};

        ConfigNode staging = new ConfigNode() {{
            child("cheese", "velveeta");
            child("amount", "99");
            child("company", new ConfigNode() {{
                child("owners", new ConfigNode() {{
                    child("0", "bobStaging");
                    child("1", "melissaStaging");
                }});
            }});
        }};

        ConfigNode production = new ConfigNode() {{
            child("cheese", "brie");
            child("amount", "200");
            child("company", new ConfigNode() {{
                child("owners", new ConfigNode() {{
                    child("0", "bobProduction");
                    child("1", "melissaProduction");
                }});
            }});
        }};

        ConfigViewImpl view = new ConfigViewImpl();

        view.withDefaults(defaults);
        view.withProperties(new Properties());
        view.register("staging", staging);
        view.register("production", production);

        view.withProfile("staging");
        view.activate();

        assertThat(view.valueOf(ConfigKey.parse("cheese"))).isEqualTo("velveeta");
        assertThat(view.valueOf(ConfigKey.parse("company.founded"))).isEqualTo("2017");
        assertThat(view.valueOf(ConfigKey.parse("company.owners.0"))).isEqualTo("bobStaging");
        assertThat(view.valueOf(ConfigKey.parse("company.owners.1"))).isEqualTo("melissaStaging");
    }

    @Test
    public void testExplicitActivationOfSeveral() {

        ConfigNode defaults = new ConfigNode() {{
            child("cheese", "cheddar");
            child("amount", "42");
            child("company", new ConfigNode() {{
                child("name", "cheeseCorp");
                child("founded", "2017");
                child("owners", new ConfigNode() {{
                    child("0", "bob");
                    child("1", "melissa");
                }});
            }});
        }};

        ConfigNode cloud = new ConfigNode() {{
            child("cheese", "velveeta");
            child("amount", "99");
            child("company", new ConfigNode() {{
                child("owners", new ConfigNode() {{
                    child("0", "bobCloud");
                }});
            }});
        }};

        ConfigNode production = new ConfigNode() {{
            child("cheese", "brie");
            child("amount", "200");
            child("company", new ConfigNode() {{
                child("owners", new ConfigNode() {{
                    child("0", "bobProduction");
                    child("1", "melissaProduction");
                }});
            }});
        }};

        ConfigViewImpl view = new ConfigViewImpl();

        view.withDefaults(defaults);
        view.withProperties(new Properties());
        view.register("cloud", cloud);
        view.register("production", production);

        view.withProfile("cloud", "production");
        view.activate();

        assertThat(view.valueOf(ConfigKey.parse("cheese"))).isEqualTo("velveeta");
        assertThat(view.valueOf(ConfigKey.parse("company.founded"))).isEqualTo("2017");
        assertThat(view.valueOf(ConfigKey.parse("company.owners.0"))).isEqualTo("bobCloud");
        assertThat(view.valueOf(ConfigKey.parse("company.owners.1"))).isEqualTo("melissaProduction");
    }
}
