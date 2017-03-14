package org.wildfly.swarm.swagger.runtime;

import java.util.Arrays;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.SwaggerMessages;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SwaggerArchivePreparer implements ArchivePreparer {
    @Override
    public void prepareArchive(Archive<?> archive) {
        if (archive instanceof JAXRSArchive) {
            // Create a JAX-RS deployment archive
            JAXRSArchive deployment = archive.as(JAXRSArchive.class);
            deployment.addModule("io.swagger");

            // Make the deployment a swagger archive
            SwaggerArchive swaggerArchive = deployment.as(SwaggerArchive.class);

            // If the context root has not been configured
            // get the context root from the deployment and tell swagger about it
            if (!swaggerArchive.hasContextRoot()) {
                swaggerArchive.setContextRoot(deployment.getContextRoot());
            }

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
                            SwaggerMessages.MESSAGES.ignoringPackage(parentPackage);
                        } else {
                            packageName = parentPackage;
                            break;
                        }
                    }
                }
                if (packageName == null) {
                    SwaggerMessages.MESSAGES.noEligiblePackages(archive.getName());
                } else {
                    SwaggerMessages.MESSAGES.configureSwaggerForPackage(archive.getName(), packageName);
                    swaggerArchive.setResourcePackages(packageName);
                }
            } else {
                SwaggerMessages.MESSAGES.configureSwaggerForSeveralPackages(archive.getName(), Arrays.asList(swaggerArchive.getResourcePackages()));
            }

            // Now add the swagger resources to our deployment
            deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
            deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        }
    }
}
