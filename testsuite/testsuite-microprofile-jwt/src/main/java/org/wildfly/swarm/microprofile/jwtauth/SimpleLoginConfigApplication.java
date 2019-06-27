package org.wildfly.swarm.microprofile.jwtauth;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("/mpjwt")
@LoginConfig(authMethod = "MP-JWT")
public class SimpleLoginConfigApplication extends Application {
    // intentionally left empty
}
