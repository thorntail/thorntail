package org.wildfly.swarm.swagger.runtime;

import io.swagger.jaxrs.config.BeanConfig;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.swagger.SwaggerFraction;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
        Map<ArchivePath, Node> map = a.getContent();
        HashSet<String> paths = new HashSet<>();
        for (Map.Entry<ArchivePath, Node> entry : map.entrySet()) {
            String path = entry.getValue().getPath().get();
            if (!path.endsWith(".class") || !path.startsWith("/WEB-INF/classes")) continue;
            path = path.replace("/WEB-INF/classes/", "");
            path = path.replaceAll("/\\w+\\$?\\w+.class", "");
            path = path.replaceAll("/", ".");
            paths.add(path);
        }
        StringBuilder pathListBuilder = new StringBuilder();
        for (String p: paths) {
            pathListBuilder.append(',').append(p);
        }
        String pathList = pathListBuilder.toString().replaceFirst(",", "");


        // Make sure the swagger resources are available
        JAXRSArchive deployment = a.as(JAXRSArchive.class);

        deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
        deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        // Ensure that all swagger dependencies are available
        deployment.addModule("org.wildfly.swarm.swagger");
        deployment.addPackage("io.swagger.models");
        deployment.addPackage("javassist.bytecode");

        String apiVersion = System.getProperty("wildfly.swarm.swagger.api.version", "1.0.0");
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(apiVersion);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setBasePath(deployment.getContextRoot());

        beanConfig.setResourcePackage(pathList);
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }
}
