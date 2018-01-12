package org.jboss.unimbus.example;

import io.restassured.RestAssured;
import org.jboss.unimbus.UNimbus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

public class ServletTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "localhost:8080";
    }

    @Ignore
    @Test
    public void test() {
        UNimbus.run(MyAppUNimbusConfig.class);

        when()
                .get("myservlet")
        .then()
                .statusCode(200)
                .body(containsString("Hello from Servlet!"));
    }
}
