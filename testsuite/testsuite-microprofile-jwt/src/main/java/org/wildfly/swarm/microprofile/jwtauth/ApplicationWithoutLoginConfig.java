package org.wildfly.swarm.microprofile.jwtauth;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("/mpjwt")
public class ApplicationWithoutLoginConfig extends Application {
    // intentionally left empty
}
