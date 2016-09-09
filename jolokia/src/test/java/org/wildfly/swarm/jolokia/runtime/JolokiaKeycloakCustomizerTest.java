package org.wildfly.swarm.jolokia.runtime;

import org.junit.Test;
import org.wildfly.swarm.jolokia.JolokiaFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class JolokiaKeycloakCustomizerTest {

    @Test
    public void testWithoutRole() {
        JolokiaFraction jolokia = new JolokiaFraction();

        JolokiaKeycloakCustomizer customizer = new JolokiaKeycloakCustomizer();

        customizer.jolokia = jolokia;

        customizer.customize();

        assertThat( jolokia.jolokiaWarPreparer() ).isNull();
    }

    @Test
    public void testWithRole() {
        JolokiaFraction jolokia = new JolokiaFraction();

        JolokiaKeycloakCustomizer customizer = new JolokiaKeycloakCustomizer();

        customizer.jolokia = jolokia;
        customizer.role = "admin";

        customizer.customize();

        assertThat( jolokia.jolokiaWarPreparer() ).isNotNull();
    }
}
