package org.wildfly.swarm.swagger.runtime;

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
    public void prepareArchive(Archive<?> a) {
        System.err.println(">>>>>>>>>>>>>>>>>>> PREPARING ARCHIVE " + a);
        JAXRSArchive deployment = a.as(JAXRSArchive.class);
        try {
            deployment.addPackage("org/wildfly.swarm.swagger");
            deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
            deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);
            deployment.addPackage("io/swagger/config");
            deployment.addPackage("io/swagger/jaxrs");
            deployment.addPackage("io/swagger/jaxrs/config");
            deployment.addPackage("io/swagger/jaxrs/listing");
            deployment.addPackage("io/swagger/annotations");
            deployment.addPackage("io/swagger/models");
            deployment.addPackage("io/swagger/models/properties");
            deployment.addPackage("io/swagger/models/parameters");
            deployment.addPackage("org/reflections");
            deployment.addPackage("org/reflections/util");
            deployment.addPackage("org/reflections/scanners");
            deployment.addPackage("org/reflections/adapters");
            deployment.addPackage("org/reflections/serializers");
            deployment.addPackage("com/google/common/base");
            deployment.addPackage("com/google/common/collect");
            deployment.addPackage("org/apache/commons/lang3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }
}
