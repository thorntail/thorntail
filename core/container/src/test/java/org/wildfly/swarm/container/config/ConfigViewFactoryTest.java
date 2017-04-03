package org.wildfly.swarm.container.config;

import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Created by bob on 4/3/17.
 */
public class ConfigViewFactoryTest {

    @Test
    public void testJohnsYaml() {
        InputStream in = getClass().getResourceAsStream("/john.yml");
        Map<String, ?> doc = ConfigViewFactory.loadYaml(in);
        Map<String, ?> foo = (Map<String, ?>) doc.get("foo");
        Map<String, ?> on = (Map<String, ?>) foo.get("on");
        assertThat((Boolean) on.get("startup")).isTrue();
    }
}
