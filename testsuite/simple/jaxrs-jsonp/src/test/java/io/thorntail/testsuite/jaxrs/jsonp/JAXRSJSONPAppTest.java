package io.thorntail.testsuite.jaxrs.jsonp;

import java.net.URL;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import io.restassured.response.Response;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.servlet.annotation.Primary;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JAXRSJSONPAppTest {

    @Test
    public void test() {
        Response response = when().get("/").andReturn();
        JsonReader reader = Json.createReader(response.asInputStream());
        JsonObject object = reader.readObject();
        assertThat(object.getInt("tacos")).isEqualTo(42);
    }

    @Inject
    @Primary
    URL url;
}
