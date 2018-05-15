package io.thorntail.testsuite.jpa;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JPAAppTest {
	@Test
	public void test() {
		String responseBody =
			when()
				.get("/")
				.then()
				.statusCode(200)
				.extract().response().body().print();

		assertThat(responseBody).isNotNull();
		assertThat(responseBody).isNotEmpty();
		assertThat(responseBody).contains("<tr><td>1</td><td>Penny</td></tr>");
		assertThat(responseBody).contains("<tr><td>2</td><td>Sheldon</td></tr>");
		assertThat(responseBody).contains("<tr><td>3</td><td>Amy</td></tr>");
		assertThat(responseBody).contains("<tr><td>4</td><td>Leonard</td></tr>");
		assertThat(responseBody).contains("<tr><td>7</td><td>Howard</td></tr>");
	}
}
