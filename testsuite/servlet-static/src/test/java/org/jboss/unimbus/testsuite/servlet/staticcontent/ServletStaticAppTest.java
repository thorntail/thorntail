package org.jboss.unimbus.testsuite.servlet.staticcontent;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jboss.unimbus.UNimbus;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(UNimbusTestRunner.class)
public class ServletStaticAppTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Test
    public void testRootServlet() {
        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello from Servlet on port 8080"));
    }

    @Test
    public void testOtherServlet() {
        when().get("/other").then()
                .statusCode(200)
                .body(containsString("Hello from other Servlet on port 8080"));
    }

    @Test
    public void testContentInPublicDir() {
        when().get("/content-public.txt").then()
                .statusCode(200)
                .body(containsString("Static content in public/"));
    }

    @Test
    public void testContentInStaticDir() {
        when().get("/content-static.txt").then()
                .statusCode(200)
                .body(containsString("Static content in static/"));
    }


    @Test
    public void testContentInMetaInfResourcesDir() {
        when().get("/content-resources.txt").then()
                .statusCode(200)
                .body(containsString("Static content in META-INF/resources/"));
    }


}
