package org.wildfly.swarm.container.config;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class EnvironmentConstructorTest {

    @Test
    public void testBasicString() {
        Map<String, String> environment = new HashMap<>();
        environment.put("VALUE", "SomeString");
        EnvironmentConstructor env = new EnvironmentConstructor(environment);
        
        assertThat( env.getValue("${env.VALUE}") ).isEqualTo("SomeString");
    }
    
    @Test
    public void testUseDefaultValue() {
        Map<String, String> environment = new HashMap<>();
        EnvironmentConstructor env = new EnvironmentConstructor(environment);
        
        assertThat( env.getValue("${env.VALUE:http://adad}") ).isEqualTo("http://adad");
    }
    @Test
    public void testUseEnvWhenDefaultExistsValue() {
        Map<String, String> environment = new HashMap<>();
        environment.put("VALUE", "SomeString");

        EnvironmentConstructor env = new EnvironmentConstructor(environment);
        
        assertThat( env.getValue("${env.VALUE:other}") ).isEqualTo("SomeString");
    }
}
