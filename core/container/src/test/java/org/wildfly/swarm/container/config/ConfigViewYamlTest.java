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

    @SuppressWarnings("unchecked")
    @Test
    public void testYamlWithLiteralLists() throws Exception {
        URL url = getClass().getClassLoader().getResource("project-lists-thorntail.yml");

        ConfigViewFactory factory = new ConfigViewFactory(new Properties());
        factory.load("test", url);

        ConfigViewImpl view = factory.get();
        view.withProfile("test");

        List<Map<?, ?>> constraints = view.resolve("thorntail.keycloak.security.constraints").as(List.class).getValue();

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0).get("url-pattern")).isEqualTo("/secured");

        List<String> methods = (List<String>) constraints.get(0).get("methods");
        assertThat(methods).hasSize(2);
        assertThat(methods).containsExactly("GET", "POST");

        List<String> roles = (List<String>) constraints.get(0).get("roles");
        assertThat(roles).hasSize(1);
        assertThat(roles).containsExactly("admin");


    }

    @SuppressWarnings("unchecked")
    @Test
    public void testYamlWithLiteralListsBackwardsCompatible() throws Exception {
        URL url = getClass().getClassLoader().getResource("project-lists-swarm.yml");

        ConfigViewFactory factory = new ConfigViewFactory(new Properties());
        factory.load("test", url);

        ConfigViewImpl view = factory.get();
        view.withProfile("test");

        List<Map<?, ?>> constraints = view.resolve("thorntail.keycloak.security.constraints").as(List.class).getValue();

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
