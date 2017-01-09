package org.wildfly.swarm.swagger.runtime;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.swagger.SwaggerArchive;

/**
 * @author Bob McWhirter
 */
@Singleton
public class SwaggerArchivePreparer implements ArchivePreparer {
    @Override
    public void prepareArchive(Archive<?> archive) {
        if (archive instanceof JAXRSArchive) {
            try {
                // Create a JAX-RS deployment archive
                JAXRSArchive deployment = archive.as(JAXRSArchive.class);
                deployment.addModule("io.swagger");

                // Make the deployment a swagger archive
                SwaggerArchive swaggerArchive = deployment.as(SwaggerArchive.class);

                // Get the context root from the deployment and tell swagger about it
                swaggerArchive.setContextRoot(deployment.getContextRoot());

                // If the archive has not been configured with packages for swagger to scan
                // try to be smart about it, and find the topmost package that's not in the
                // org.wildfly.swarm package space
                if (!swaggerArchive.hasResourcePackages()) {
                    String packageName = null;
                    for (Map.Entry<ArchivePath, Node> entry : deployment.getContent().entrySet()) {
                        final ArchivePath key = entry.getKey();
                        if (key.get().endsWith(".class")) {
                            String parentPath = key.getParent().get();
                            parentPath = parentPath.replaceFirst("/", "");

                            String parentPackage = parentPath.replaceFirst(".*/classes/", "");
                            parentPackage = parentPackage.replaceAll("/", ".");

                            if (parentPackage.startsWith("org.wildfly.swarm")) {
                                System.out.println("[Swagger] Ignoring swarm package " + parentPackage);
                            } else {
                                packageName = parentPackage;
                                break;
                            }
                        }
                    }
                    if (packageName == null) {
                        System.err.println("[Swagger] No eligible packages for Swagger to scan.");
                    } else {
                        System.out.println("[Swagger] Configuring Swagger with package " + packageName);
                        swaggerArchive.setResourcePackages(packageName);
                    }
                } else {
                    System.out.println("[SWAGGER] Configuring Swagger with packages " + Arrays.toString(swaggerArchive.getResourcePackages()));
                }

                // Now add the swagger resources to our deployment
                deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
                deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
