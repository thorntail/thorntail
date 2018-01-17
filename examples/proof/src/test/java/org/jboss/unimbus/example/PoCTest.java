package org.jboss.unimbus.example;

import io.restassured.RestAssured;
import org.jboss.unimbus.UNimbus;
import org.junit.BeforeClass;
import org.junit.Test;


import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

public class PoCTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    public void test() {
        UNimbus.run();

        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello! 8080"));
    }
}
