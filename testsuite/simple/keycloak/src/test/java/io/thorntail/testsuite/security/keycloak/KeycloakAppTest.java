package io.thorntail.testsuite.security.keycloak;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(ThorntailTestRunner.class)
public class KeycloakAppTest {

    @Test
    public void test() {
        // 401 would be better but at the moment returning null from KeycloakConfigResolver causes KC NPE and 500
        when().get("/secured").then()
                .statusCode(500);
        
        when().get("/unsecured").then()
                .statusCode(200)
                .body(containsString("Hello from JAX-RS"));
    }
}
