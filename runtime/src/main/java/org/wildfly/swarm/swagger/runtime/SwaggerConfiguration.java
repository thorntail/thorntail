package org.wildfly.swarm.swagger.runtime;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.container.runtime.Configuration;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.swagger.SwaggerFraction;

import java.util.Collections;
import java.util.List;

/**
 * @author Lance Ball
 */
@Configuration
public class SwaggerConfiguration extends AbstractServerConfiguration<SwaggerFraction> {

    public SwaggerConfiguration() {
        super(SwaggerFraction.class);
    }

    @Override
    public SwaggerFraction defaultFraction() {
        return new SwaggerFraction();
    }


    @Override
    public void prepareArchive(Archive<?> a) {
        JAXRSArchive deployment = a.as(JAXRSArchive.class);
        JARArchive archive = a.as(JARArchive.class);
        try {
            archive.addModule("io.swagger");
            deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
            deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }
}
