package org.wildfly.swarm.keycloak;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class SecuredApplication extends Application {
}
