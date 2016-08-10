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
package org.wildfly.swarm.management.console.runtime;

import java.net.URL;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.management.console.ManagementConsoleFraction;
import org.wildfly.swarm.management.console.ManagementConsoleProperties;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
@Singleton
public class ManagementConsoleDeploymentProducer {

    @Inject
    private ArtifactLookup lookup;

    @Inject @Any
    private ManagementConsoleFraction fraction;

    @Inject
    @ConfigurationValue(ManagementConsoleProperties.CONTEXT)
    private String context;

    @Produces
    public Archive managementConsoleWar() throws Exception {
        if ( this.context == null ) {
            this.context = fraction.contextRoot();
        }

        // Load the swagger-ui webjars.
        Module module = Module.getBootModuleLoader().loadModule( ModuleIdentifier.create( "org.jboss.as.console" ) );
        URL resource = module.getExportedResource("jboss-as-console-resources.jar");
        WARArchive war = ShrinkWrap.create(WARArchive.class, "management-console-ui.war" );
        war.as(ZipImporter.class).importFrom( resource.openStream() );

        war.setContextRoot( this.context );

        return war;
    }

}
