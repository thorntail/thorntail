package io.thorntail.example.testsuite.proof;

import javax.inject.Inject;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.thorntail.test.EphemeralPorts;
import io.thorntail.test.ThorntailTestRunner;
import io.thorntail.security.basic.BasicSecurity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;

@RunWith(ThorntailTestRunner.class)
@EphemeralPorts
public class ProofAppTest {

    @Before
    public void setup() {
        security.addUser("bob", "password");
    }

    @Test
    public void test() {
        System.err.println( "---> " + RestAssured.baseURI);
        Response response = when().get("/").andReturn();
        System.err.println("response: " + response.asString());
    }

    @Inject
    BasicSecurity security;
}
