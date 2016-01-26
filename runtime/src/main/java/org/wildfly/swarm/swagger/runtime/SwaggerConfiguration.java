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

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
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

        try {
            JARArchive jarArchive = a.as(JARArchive.class);
            jarArchive.addModule("io.swagger");

            JAXRSArchive deployment = a.as(JAXRSArchive.class);
            deployment.addResource(io.swagger.jaxrs.listing.ApiListingResource.class);
            deployment.addResource(io.swagger.jaxrs.listing.SwaggerSerializers.class);

            // Set only required configuration option for swagger
            SwaggerArchive swaggerArchive = jarArchive.as(SwaggerArchive.class);
            swaggerArchive.setContextRoot(deployment.getContextRoot());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ModelNode> getList(SwaggerFraction fraction) throws Exception {
        return Collections.emptyList();
    }
}
