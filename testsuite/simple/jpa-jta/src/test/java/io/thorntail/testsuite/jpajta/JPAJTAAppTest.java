package io.thorntail.testsuite.jpajta;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * Test {@link javax.transaction.Transactional} with rollback and without rollback on exception
 *
 * @author Antoine Sabot-Durand
 *
 */
@RunWith(ThorntailTestRunner.class)
public class JPAJTAAppTest {

    @Test
    public void test() {
        String responseBody =
                when()
                        .get("/remwrb/3")
                        .then()
                        .statusCode(500)
                        .extract().response().body().print();

        assertThat(responseBody).contains("Internal Server Error");



        responseBody =
                when()
                        .get("/all")
                        .then()
                        .statusCode(200)
                        .extract().response().body().print();

        assertThat(responseBody).isNotEmpty();
        for (Integer i = 1; i < 9; i++) {
            assertThat(responseBody).contains(i.toString());
        }

        responseBody =
                when()
                        .get("/remworb/7")
                        .then()
                        .statusCode(500)
                        .extract().response().body().print();

        assertThat(responseBody).contains("Internal Server Error");


        responseBody =
                when()
                        .get("/all")
                        .then()
                        .statusCode(200)
                        .extract().response().body().print();
        assertThat(responseBody).isNotEmpty();
        for (Integer i = 1; i < 7; i++) {
            assertThat(responseBody).doesNotContain(i.toString());
        }
        assertThat(responseBody).contains("8");

    }
}
