package io.thorntail.testsuite.servlet.staticcontent;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(ThorntailTestRunner.class)
public class ServletStaticAppTest {

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
    public void testContentInStaticDirWithWelcomeFileWithTrailingSlash() {
        when().get("/bar/").then()
                .statusCode(200)
                .body(containsString("This is bar/index.html"));
    }

    @Test
    public void testContentInStaticDirWithWelcomeFileWithoutTrailingSlash() {
        when().get("/bar").then()
                .statusCode(200)
                .body(containsString("This is bar/index.html"));
    }


    @Test
    public void testContentInMetaInfResourcesDir() {
        when().get("/content-resources.txt").then()
                .statusCode(200)
                .body(containsString("Static content in META-INF/resources/"));
    }


}
