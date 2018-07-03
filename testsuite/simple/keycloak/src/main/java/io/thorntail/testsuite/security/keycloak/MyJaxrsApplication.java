package io.thorntail.testsuite.security.keycloak;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/secured")
@LoginConfig(authMethod = "KEYCLOAK")
public class MyJaxrsApplication extends Application {
}
