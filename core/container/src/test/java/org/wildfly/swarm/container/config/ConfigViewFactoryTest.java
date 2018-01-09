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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

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

    @Test
    public void testLucasesYaml() {
        InputStream in = getClass().getResourceAsStream("/lucas.yml");
        Map<String, ?> doc = ConfigViewFactory.loadYaml(in);
        Map<String, ?> swarm = (Map<String, ?>) doc.get("swarm");
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
}
