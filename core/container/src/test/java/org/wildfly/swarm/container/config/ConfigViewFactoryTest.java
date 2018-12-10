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

import static org.fest.assertions.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
/**
 * Created by bob on 4/3/17.
 */
public class ConfigViewFactoryTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testJohnsYaml() {
        InputStream in = getClass().getResourceAsStream("/john.yml");
        Map<String, ?> doc = ConfigViewFactory.loadYaml(in);
        Map<String, ?> foo = (Map<String, ?>) doc.get("foo");
        Map<String, ?> on = (Map<String, ?>) foo.get("on");
        assertThat((Boolean) on.get("startup")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLucasesYaml() {
        InputStream in = getClass().getResourceAsStream("/lucas.yml");
        Map<String, ?> doc = ConfigViewFactory.loadYaml(in);
        Map<String, ?> swarm = (Map<String, ?>) doc.get("thorntail");
        Map<String, ?> security = (Map<String, ?>) swarm.get("security");
        Map<String, ?> securityDomains = (Map<String, ?>) security.get("security-domains");
        Map<String, ?> jaspioauth = (Map<String, ?>) securityDomains.get("jaspioauth");
        Map<String, ?> jaspiAuthentication = (Map<String, ?>) jaspioauth.get("jaspi-authentication");
        Map<String, ?> authModules = (Map<String, ?>) jaspiAuthentication.get("auth-modules");

        Set<String> keys = authModules.keySet();
        Iterator<String> keysIter = keys.iterator();
        assertThat( keysIter.next() ).isEqualTo("2-OAuth2");
        assertThat( keysIter.next() ).isEqualTo("1-JWT");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testYamlWithEnvProperties() {
        InputStream in = getClass().getResourceAsStream("/withenvvalues.yml");
        Map<String, String> environment = new HashMap<>();
        environment.put("BOOL", "true");
        environment.put("LOG_NAME", "SomeString");
        Map<String, ?> doc = ConfigViewFactory.loadYaml(in,environment);
        Map<String, ?> foo = (Map<String, ?>) doc.get("foo");
        Map<String, ?> on = (Map<String, ?>) foo.get("on");
        assertThat(Boolean.valueOf((String)on.get("startup"))).isTrue();
        assertThat( on.get("somestring") ).isEqualTo("http://someurl");
    }

    @Test
    public void testPropertyHierarchy() {
        // Given
        ConfigViewFactory configViewFactory = new ConfigViewFactory(new Properties());

        // When
        configViewFactory.withProperty("parent.children", "child1,child2");
        configViewFactory.withProperty("parent.children.child1.name", "Jill");
        configViewFactory.withProperty("parent.children.child2.name", "Jack");

        // Then
        List<String> keys = new LinkedList<>();
        configViewFactory.get().allKeysRecursively().forEach(key -> keys.add(key.name()));
        assertTrue("parent.children not found in properties", keys.contains("parent.children"));
        assertTrue("parent.children.child1.name not found in properties", keys.contains("parent.children.child1.name"));
        assertTrue("parent.children.child2.name not found in properties", keys.contains("parent.children.child2.name"));
    }

    @Test
    public void testPropertyHierarchy_reverseOrder() {
        // Given
        ConfigViewFactory configViewFactory = new ConfigViewFactory(new Properties());

        // When
        configViewFactory.withProperty("parent.children.child1.name", "Jill");
        configViewFactory.withProperty("parent.children.child2.name", "Jack");
        configViewFactory.withProperty("parent.children", "child1,child2");

        // Then
        List<String> keys = new LinkedList<>();
        configViewFactory.get().allKeysRecursively().forEach(key -> keys.add(key.name()));
        assertTrue("parent.children not found in properties", keys.contains("parent.children"));
        assertTrue("parent.children.child1.name not found in properties", keys.contains("parent.children.child1.name"));
        assertTrue("parent.children.child2.name not found in properties", keys.contains("parent.children.child2.name"));
    }
}
