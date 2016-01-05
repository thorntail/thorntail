package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.Assignable;

/**
 * @author Lance Ball
 */
public interface SwaggerArchive extends Assignable {
    String SWAGGER_CONFIGURATION_PATH = "META-INF/swagger.conf";

    public SwaggerArchive register(String... packages);
}
