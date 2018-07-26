package org.wildfly.swarm.microprofile.jwtauth.roles;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("/mpjwt")
@LoginConfig(authMethod = "MP-JWT", realmName = "testSuiteRealm")
// TODO: remove? this class is exactly the same as org.wildfly.swarm.microprofile.jwtauth.TestApplication
public class TestApplication extends Application {
    // intentionally left empty
}

