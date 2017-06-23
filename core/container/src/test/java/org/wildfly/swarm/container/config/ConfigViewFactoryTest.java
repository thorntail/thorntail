package org.wildfly.swarm.container.config;

import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
