package org.jboss.unimbus.testsuite.security.keycloak;

import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(UNimbusTestRunner.class)
public class KeycloakAppTest {

    @Test
    public void test() {
        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello from JAX-RS"));
    }
}
