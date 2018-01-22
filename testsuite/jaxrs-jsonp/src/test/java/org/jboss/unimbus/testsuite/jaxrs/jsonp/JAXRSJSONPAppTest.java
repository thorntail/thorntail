package org.jboss.unimbus.testsuite.jaxrs.jsonp;

import java.net.URL;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jboss.unimbus.servlet.Primary;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(UNimbusTestRunner.class)
public class JAXRSJSONPAppTest {

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @After
    public void teardown() {
        RestAssured.reset();
    }

    @org.junit.Test
    public void test() {
        Response response = when().get("/").andReturn();
                //.statusCode(200)
                //.body(containsString("Hello from JAX-RS"));
        JsonReader reader = Json.createReader(response.asInputStream());
        JsonObject object = reader.readObject();
        assertThat( object.getInt("tacos")).isEqualTo(42);
    }

    @Inject
    @Primary
    URL url;
}
