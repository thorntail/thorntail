package org.wildfly.swarm.container.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.SimpleKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigNodeTest {

    @Test
    public void testValueOf_flat() {
        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");

        assertThat(config.valueOf(new SimpleKey("cheese"))).isEqualTo("cheddar");
        assertThat(config.valueOf(new SimpleKey("amount"))).isEqualTo("42");
    }

    @Test
    public void testValueOf_nested() {

        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");
        config.child("company", new ConfigNode() {{
            child("name", "cheeseCorp");
            child("founded", "2017");
            child("owners", new ConfigNode() {{
                child("0", "bob");
                child("1", "melissa");
            }});
        }});

        assertThat(config.valueOf(ConfigKey.parse("cheese"))).isEqualTo("cheddar");
        assertThat(config.valueOf(ConfigKey.parse("amount"))).isEqualTo("42");

        assertThat(config.valueOf(ConfigKey.parse("company.name"))).isEqualTo("cheeseCorp");
        assertThat(config.valueOf(ConfigKey.parse("company.founded"))).isEqualTo("2017");

        assertThat(config.valueOf(ConfigKey.parse("company.owners.0"))).isEqualTo("bob");
        assertThat(config.valueOf(ConfigKey.parse("company.owners.1"))).isEqualTo("melissa");

    }

    @Test
    public void testAllKeysRecursively_flat() {
        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");

        Set<ConfigKey> keys = config.allKeysRecursively().collect(Collectors.toSet());

        assertThat(keys).containsOnly(
                ConfigKey.parse("cheese"),
                ConfigKey.parse("amount")
        );
    }

    @Test
    public void testAllKeysRecursively_nested() {

        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");
        config.child("company", new ConfigNode() {{
            child("name", "cheeseCorp");
            child("founded", "2017");
            child("owners", new ConfigNode() {{
                child("0", "bob");
                child("1", "melissa");
            }});
        }});

        Set<ConfigKey> keys = config.allKeysRecursively().collect(Collectors.toSet());

        assertThat(keys).containsOnly(
                ConfigKey.parse("cheese"),
                ConfigKey.parse("amount"),
                ConfigKey.parse("company.name"),
                ConfigKey.parse("company.founded"),
                ConfigKey.parse("company.owners.0"),
                ConfigKey.parse("company.owners.1")
        );
    }


    @Test
    public void testRecursiveChild_flat() {
        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");

        Set<ConfigKey> keys = config.allKeysRecursively().collect(Collectors.toSet());

        assertThat(keys).containsOnly(
                ConfigKey.parse("cheese"),
                ConfigKey.parse("amount")
        );

        config.recursiveChild(ConfigKey.parse("cheese"), "brie");

        assertThat(config.valueOf(ConfigKey.parse("cheese"))).isEqualTo("brie");
    }

    @Test
    public void testRecursiveChild_nested() {

        ConfigNode config = new ConfigNode();

        config.child("cheese", "cheddar");
        config.child("amount", "42");
        config.child("company", new ConfigNode() {{
            child("name", "cheeseCorp");
            child("founded", "2017");
            child("owners", new ConfigNode() {{
                child("0", "bob");
                child("1", "melissa");
            }});
        }});

        assertThat(config.valueOf(ConfigKey.parse("company.name"))).isEqualTo("cheeseCorp");
        config.recursiveChild(ConfigKey.parse("company.name"), "Wheel O'Cheese");
        assertThat(config.valueOf(ConfigKey.parse("company.name"))).isEqualTo("Wheel O'Cheese");
    }

    @Test
    public void testOrderingOfChildren() {
        ConfigNode config = new ConfigNode();

        config.child( "Z", "26");
        config.child( "A", "1");

        List<SimpleKey> keys = new ArrayList<>();
        keys.addAll( config.childrenKeys() );

        assertThat( keys.get(0).toString()).isEqualTo("Z");
        assertThat( keys.get(1).toString()).isEqualTo("A");
    }

    @Test
    public void testOrderingOfChildrenRecursively() {
        ConfigNode config = new ConfigNode();

        config.child( "Z", "26");
        config.child( "A", "1");

        List<ConfigKey> keys = config.allKeysRecursively().collect(Collectors.toList());

        assertThat( keys.get(0).toString()).isEqualTo("Z");
        assertThat( keys.get(1).toString()).isEqualTo("A");
    }
}
