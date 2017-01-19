package org.wildfly.swarm.container.config;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.CompositeKey;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.SimpleKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigKeyTest {

    @Test
    public void testParseWithOnlyDots() {
        ConfigKey key = ConfigKey.parse("foo.bar.baz");

        assertThat(key.head().name()).isEqualTo("foo");
        assertThat(key.subkey(1).head().name()).isEqualTo("bar");
        assertThat(key.subkey(2).head().name()).isEqualTo("baz");
        assertThat(key.subkey(3).head()).isEqualTo(ConfigKey.EMPTY);

        assertThat(key.name()).isEqualTo("foo.bar.baz");
        assertThat(key).isInstanceOf(CompositeKey.class);
    }

    @Test
    public void testOneSegmentInsideDelim() {
        ConfigKey key = ConfigKey.parse("[foo]");
        assertThat(key.head().name()).isEqualTo("foo");

        assertThat(key.name()).isEqualTo("foo");
        assertThat(key).isInstanceOf(SimpleKey.class);
    }


    @Test
    public void testSeveralSegmentsNoInternalDots() {
        ConfigKey key = ConfigKey.parse("[foo].[bar].[baz]");

        assertThat(key.head().name()).isEqualTo("foo");
        assertThat(key.subkey(1).head().name()).isEqualTo("bar");
        assertThat(key.subkey(2).head().name()).isEqualTo("baz");
        assertThat(key.subkey(3).head()).isEqualTo(ConfigKey.EMPTY);

        assertThat(key.name()).isEqualTo("foo.bar.baz");
    }

    @Test
    public void testSeveralSegmentsWithInternalDots() {
        ConfigKey key = ConfigKey.parse("foo.[bar.baz].taco");

        assertThat(key.head().name()).isEqualTo("foo");
        assertThat(key.subkey(1).head().name()).isEqualTo("bar.baz");
        assertThat(key.subkey(2).head().name()).isEqualTo("taco");
        assertThat(key.subkey(3).head()).isEqualTo(ConfigKey.EMPTY);

        assertThat(key.name()).isEqualTo("foo.[bar.baz].taco");
    }
}

