package org.wildfly.swarm.container.config;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigViewYamlTest {

    @Test
    public void testYamlWithLiteralLists() throws Exception {
        URL url = getClass().getClassLoader().getResource("project-lists.yml");

        ConfigViewFactory factory = new ConfigViewFactory(new Properties());
        factory.load("test", url);

        ConfigViewImpl view = factory.build();
        view.activate("test");

        List<Map<?, ?>> constraints = view.resolve("swarm.keycloak.security.constraints").as(List.class).getValue();

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0).get("url-pattern")).isEqualTo("/secured");

        List<String> methods = (List<String>) constraints.get(0).get("methods");
        assertThat(methods).hasSize(2);
        assertThat(methods).containsExactly("GET", "POST");

        List<String> roles = (List<String>) constraints.get(0).get("roles");
        assertThat(roles).hasSize(1);
        assertThat(roles).containsExactly("admin");


    }
}
