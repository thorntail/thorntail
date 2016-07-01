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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.management.console.ManagementConsoleFraction;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author George Gastaldi
 */
public class ManagementConsoleConfiguration extends AbstractServerConfiguration<ManagementConsoleFraction> {

    public ManagementConsoleConfiguration() {
        super(ManagementConsoleFraction.class);
    }

    @Override
    public List<Archive> getImplicitDeployments(ManagementConsoleFraction fraction) throws Exception {
        WARArchive war = ArtifactLookup.get()
                .artifact("org.jboss.as:jboss-as-console:jar:2.8.25.Final:resources", "management-console-ui.war")
                .as(WARArchive.class)
                .setContextRoot(fraction.getContextRoot());
        return Collections.singletonList(war);
    }

    @Override
    public ManagementConsoleFraction defaultFraction() {
        return new ManagementConsoleFraction();
    }
}
