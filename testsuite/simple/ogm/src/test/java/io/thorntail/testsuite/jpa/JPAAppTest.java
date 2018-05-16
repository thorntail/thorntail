package io.thorntail.testsuite.jpa;

import java.io.File;
import java.io.IOException;

import io.thorntail.test.ThorntailTestRunner;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ThorntailTestRunner.class)
public class JPAAppTest {
	@AfterClass
	public static void cleanup() throws IOException {
        deleteDirectory(new File("target", "neo4j.db"));
	}

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

    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
