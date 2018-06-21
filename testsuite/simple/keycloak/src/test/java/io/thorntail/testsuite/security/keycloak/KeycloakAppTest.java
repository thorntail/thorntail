package io.thorntail.testsuite.security.keycloak;

import static io.restassured.RestAssured.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.thorntail.test.ThorntailTestRunner;

@RunWith(ThorntailTestRunner.class)
public class KeycloakAppTest {

    @Test
    public void test() {
        // 401 would be better but at the moment returning null from KeycloakConfigResolver causes KC NPE and 500
        when().get("/").then()
                .statusCode(500);
    }
}
