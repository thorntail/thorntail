package org.jboss.unimbus.example.testsuite.proof;

import java.net.URL;

import javax.inject.Inject;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jboss.unimbus.security.basic.BasicSecurity;
import org.jboss.unimbus.servlet.Primary;
import org.jboss.unimbus.test.UNimbusTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.when;

@RunWith(UNimbusTestRunner.class)
public class ProofAppTest {

    @Before
    public void setup() {
        RestAssured.baseURI = this.url.toExternalForm();
        security.addUser("bob", "password");
    }

    @After
    public void teardown() {
        RestAssured.reset();
    }

    @Test
    public void test() {
        Response response = when().get("/").andReturn();
        System.err.println( "response: " + response.asString());
    }

    @Inject
    @Primary
    URL url;

    @Inject
    BasicSecurity security;
}
