package org.jboss.unimbus.example.testsuite.proof;

import javax.inject.Inject;

import io.restassured.response.Response;
import org.jboss.unimbus.security.basic.BasicSecurity;
import org.jboss.unimbus.test.EphemeralPorts;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;

@RunWith(UNimbusTestRunner.class)
@EphemeralPorts
public class ProofAppTest {

    @Before
    public void setup() {
        security.addUser("bob", "password");
    }

    @Test
    public void test() {
        Response response = when().get("/").andReturn();
        System.err.println("response: " + response.asString());
    }

    @Inject
    BasicSecurity security;
}
