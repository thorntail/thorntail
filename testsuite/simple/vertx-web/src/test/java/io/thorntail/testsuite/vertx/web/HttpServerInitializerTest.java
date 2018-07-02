package io.thorntail.testsuite.vertx.web;

import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.thorntail.test.ThorntailTestRunner;

@RunWith(ThorntailTestRunner.class)
public class HttpServerInitializerTest {

    @Test
    public void testHandler() throws InterruptedException {
        RestAssured.given().get("/hello").then().assertThat().statusCode(200).body(equalTo("foo"));
    }

    @Test
    public void testBlockingHandler() throws InterruptedException {
        RestAssured.given().get("/helloBlocking").then().assertThat().statusCode(200).body(equalTo("foo"));
    }

    @Test
    public void testProgrammaticHandler() throws InterruptedException {
        RestAssured.given().get("/next").then().assertThat().statusCode(200).body(equalTo("nextOne"));
    }

    @Test
    public void testObserver() throws InterruptedException {
        RestAssured.given().get("/helloObserver").then().assertThat().statusCode(200).body(equalTo("foo:observer"));
    }

}