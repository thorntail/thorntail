/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.swagger.runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.swagger.SwaggerArchive;
import org.wildfly.swarm.swagger.SwaggerFraction;

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
        if(a instanceof JAXRSArchive) {
            try {
                // Create a JAX-RS deployment archive
                JAXRSArchive deployment = a.as(JAXRSArchive.class);
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
                }
                else {
                    System.out.println("[SWAGGER] Configuring Swagger with packages "+ Arrays.toString(swaggerArchive.getResourcePackages()));
                }

                // Now add the swagger resources to our deployment
                deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
                deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }

}
