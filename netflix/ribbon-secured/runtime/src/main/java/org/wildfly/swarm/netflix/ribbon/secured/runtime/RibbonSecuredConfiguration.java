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
package org.wildfly.swarm.netflix.ribbon.secured.runtime;

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.netflix.ribbon.secured.RibbonSecuredFraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;

/**
 * @author Bob McWhirter
 */
public class RibbonSecuredConfiguration extends AbstractServerConfiguration<RibbonSecuredFraction> {

    public RibbonSecuredConfiguration() {
        super(RibbonSecuredFraction.class);
    }

    @Override
    public RibbonSecuredFraction defaultFraction() {
        return new RibbonSecuredFraction();
    }

    @Override
    public List<ModelNode> getList(RibbonSecuredFraction fraction) {
        return Collections.emptyList();
    }

    @Override
    public void prepareArchive(Archive<?> archive) {
        archive.as(JARArchive.class).addModule("org.wildfly.swarm.netflix.ribbon.secured", "client");
    }
}
