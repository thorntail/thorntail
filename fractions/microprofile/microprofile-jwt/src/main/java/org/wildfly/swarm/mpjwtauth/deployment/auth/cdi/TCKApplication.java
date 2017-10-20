package org.wildfly.swarm.mpjwtauth.deployment.auth.cdi;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

/**
 * A JAX-RS application marked as requiring MP-JWT authentication
 */
@LoginConfig(authMethod = "MP-JWT", realmName = "TCK-MP-JWT")
@ApplicationPath("/")
public class TCKApplication extends Application {
}
