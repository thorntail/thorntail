package org.jboss.unimbus.testsuite.servlet;

import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(UNimbusTestRunner.class)
public class ServletAppTest {

    @Test
    public void test() {
        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello from Servlet on port 8080"));

        when().get("/other").then()
                .statusCode(200)
                .body(containsString("Hello from other Servlet on port 8080"));
    }
}
