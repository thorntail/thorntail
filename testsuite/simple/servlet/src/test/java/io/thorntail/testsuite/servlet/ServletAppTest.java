package io.thorntail.testsuite.servlet;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(ThorntailTestRunner.class)
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
