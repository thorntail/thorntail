package org.wildfly.swarm.swagger.runtime;

import io.swagger.jaxrs.config.BeanConfig;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.swagger.SwaggerFraction;

import java.util.Collections;
import java.util.List;

/**
 * @author Lance Ball
 */
public class SwaggerConfiguration extends AbstractServerConfiguration<SwaggerFraction> {
    public SwaggerConfiguration() {
        super(SwaggerFraction.class);
    }

    @Override
    public SwaggerFraction defaultFraction() {
        return new SwaggerFraction();
    }

    @Override
    public void prepareArchive(Archive a) {
        // Make sure the swagger resources are available
        JAXRSArchive deployment = a.as(JAXRSArchive.class);
        deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
        deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        // Ensure that all swagger dependencies are available
        deployment.addModule("org.wildfly.swarm.swagger");
        deployment.addPackage("io.swagger.models");

        String apiVersion = System.getProperty("wildfly.swarm.swagger.api.version", "1.0.0");
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(apiVersion);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setBasePath(deployment.getContextRoot());

        beanConfig.setResourcePackage("io.swagger.resources");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }
}
