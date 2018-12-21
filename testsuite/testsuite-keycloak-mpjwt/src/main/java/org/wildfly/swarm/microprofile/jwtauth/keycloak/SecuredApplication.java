package org.wildfly.swarm.microprofile.jwtauth.keycloak;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

//@ApplicationScoped
@ApplicationPath("/mpjwt")
@LoginConfig(authMethod = "MP-JWT", realmName = "testSuiteRealm")
public class SecuredApplication extends Application {
    // intentionally left empty
}

