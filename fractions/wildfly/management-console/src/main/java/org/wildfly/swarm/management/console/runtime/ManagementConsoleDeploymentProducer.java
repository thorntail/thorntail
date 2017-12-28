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

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.modules.Module;
import org.jboss.modules.Resource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.wildfly.swarm.management.console.ManagementConsoleFraction;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class ManagementConsoleDeploymentProducer {

    @Inject
    private ArtifactLookup lookup;

    @Inject
    @Any
    private ManagementConsoleFraction fraction;

    @Produces
    public Archive managementConsoleWar() throws Exception {
        // Load the management-ui webjars.
        WARArchive war = ShrinkWrap.create(WARArchive.class, "management-console-ui.war");
        Module module = Module.getBootModuleLoader().loadModule("org.jboss.as.console");
        Iterator<Resource> resources = module.globResources("*");
        while (resources.hasNext()) {
            Resource each = resources.next();
            war.add(new UrlAsset(each.getURL()), each.getName());
        }
        war.setContextRoot(this.fraction.contextRoot());
        return war;
    }

}
