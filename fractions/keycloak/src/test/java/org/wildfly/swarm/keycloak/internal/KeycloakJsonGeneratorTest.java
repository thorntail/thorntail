package org.wildfly.swarm.keycloak.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.adapters.config.AdapterConfig;

import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.keycloak.internal.KeycloakJsonGenerator.PREFIX;

public class KeycloakJsonGeneratorTest {

    private static final String REALM = PREFIX + "realm";
    private static final String CREDENTIALS = PREFIX + "credentials";
    private static final String CONNECTION_POOL_SIZE = PREFIX + "connection-pool-size";
    private static final String ENABLE_CORS = PREFIX + "enable-cors";

    @Before
    public void setUp() throws Exception {
        System.setProperty(REALM, "booker");
        System.setProperty(CREDENTIALS, "secret=deec63e4-d242-4180-b402-80fba0a9187e");
        System.setProperty(CONNECTION_POOL_SIZE, "100");
        System.setProperty(ENABLE_CORS, "true");
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(REALM);
        System.clearProperty(CREDENTIALS);
        System.clearProperty(CONNECTION_POOL_SIZE);
        System.clearProperty(ENABLE_CORS);
    }

    @Test
    public void test_generated_keycloak_json_from_properties() throws Exception {
        StringBuilder keycloakJson = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(KeycloakJsonGenerator.generate()))) {
            reader.lines().forEach(keycloakJson::append);
        }
        AdapterConfig result = new ObjectMapper().readValue(keycloakJson.toString(), AdapterConfig.class);

        assertThat(result.getRealm()).isEqualTo("booker");
        assertThat(result.getCredentials().get("secret")).isEqualTo("deec63e4-d242-4180-b402-80fba0a9187e");
        assertThat(result.getConnectionPoolSize()).isEqualTo(100);
        assertThat(result.isCors()).isEqualTo(true);
    }

}