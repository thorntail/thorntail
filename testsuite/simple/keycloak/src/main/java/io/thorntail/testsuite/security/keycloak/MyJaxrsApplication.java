package io.thorntail.testsuite.security.keycloak;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

import io.thorntail.Thorntail;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/")
@LoginConfig(authMethod = "KEYCLOAK")
public class MyJaxrsApplication extends Application {
    public static void main(String... args) throws Exception {
        Thorntail.run();
    }
}
