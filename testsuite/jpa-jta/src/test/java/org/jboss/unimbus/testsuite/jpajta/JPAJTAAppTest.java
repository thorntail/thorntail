package org.jboss.unimbus.testsuite.jpajta;

import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.fest.assertions.Assertions.assertThat;

/**
 *
 * Test {@link javax.transaction.Transactional} with rollback and without rollback on exception
 *
 * @author Antoine Sabot-Durand
 *
 */
@RunWith(UNimbusTestRunner.class)
public class JPAJTAAppTest {

    @Test
    public void test() {
        String responseBody =
                when()
                        .get("/remwrb/3")
                        .then()
                        .statusCode(500)
                        .extract().response().body().print();

        assertThat(responseBody).isEmpty();



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

        assertThat(responseBody).isEmpty();


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
