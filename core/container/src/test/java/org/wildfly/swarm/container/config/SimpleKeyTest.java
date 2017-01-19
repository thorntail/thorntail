package org.wildfly.swarm.container.config;

import org.junit.Test;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.SimpleKey;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class SimpleKeyTest {

    @Test
    public void testHead() {
        SimpleKey key = new SimpleKey("hi");
        assertThat(key.head()).isEqualTo(key);
    }

    @Test
    public void testSubkey() {
        SimpleKey key = new SimpleKey("hi");
        assertThat(key.subkey(0)).isEqualTo(key);
        assertThat(key.subkey(1)).isEqualTo(ConfigKey.EMPTY);
    }

}
