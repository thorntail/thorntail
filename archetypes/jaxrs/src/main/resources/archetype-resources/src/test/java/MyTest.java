package ${package};

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(ThorntailTestRunner.class)
public class MyTest {

    @Test
    public void test() {
        when().get("/").then()
                .statusCode(200)
                .body(containsString("Hello World"));
    }
}

