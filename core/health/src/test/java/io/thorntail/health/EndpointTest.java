package io.thorntail.health;

import java.net.URL;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.servlet.annotation.Management;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class EndpointTest {

    @Before
    public void setup() {
        RestAssured.baseURI = this.url.toExternalForm();
    }

    @After
    public void teardown() {
        RestAssured.reset();
    }

    @Test
    public void test() {
        Response response = when().get("/health").andReturn();

        JsonReader reader = Json.createReader(response.asInputStream());
        JsonObject root = reader.readObject();

        assertThat(root.getString("outcome")).isEqualTo("UP");

        JsonArray checks = root.getJsonArray("checks");
        assertThat(checks.size()).isGreaterThan(0);

        checks.forEach(check -> {
            assertThat(((JsonObject) check).getString("name")).isEqualTo("test-check");
            assertThat(((JsonObject) check).getString("state")).isEqualTo("UP");
        });
    }

    @Inject
    @Management
    URL url;
    
    @Health
    @Produces
    public HealthCheck check() {
    	return new TestHealthCheck();
    }
}
