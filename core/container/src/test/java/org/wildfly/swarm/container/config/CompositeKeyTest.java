package org.wildfly.swarm.container.config;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.CompositeKey;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.SimpleKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class CompositeKeyTest {

    @Test
    public void testHead() {
        CompositeKey key = new CompositeKey("foo", "bar", "baz");
        assertThat(key.head()).isEqualTo(new SimpleKey("foo"));
    }

    @Test
    public void testSubkey() {
        CompositeKey key = new CompositeKey("foo", "bar", "baz");
        assertThat(key.subkey(1).head()).isEqualTo(new SimpleKey("bar"));
        assertThat(key.subkey(2).head()).isEqualTo(new SimpleKey("baz"));
        assertThat(key.subkey(3).head()).isEqualTo(ConfigKey.EMPTY);
    }

    @Test
    public void testName() {
        CompositeKey key = new CompositeKey("foo", "bar", "baz");
        assertThat(key.name()).isEqualTo("foo.bar.baz");
    }
}
