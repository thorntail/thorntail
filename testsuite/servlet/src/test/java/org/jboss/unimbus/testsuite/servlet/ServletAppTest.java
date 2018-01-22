package org.jboss.unimbus.testsuite.servlet;

import io.restassured.RestAssured;
import org.jboss.unimbus.UNimbus;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

public class ServletAppTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    public void test() {
        UNimbus.run();

        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello from Servlet on port 8080"));

        when().get("/other").then()
                .statusCode(200)
                .body(containsString("Hello from other Servlet on port 8080"));
    }
}
