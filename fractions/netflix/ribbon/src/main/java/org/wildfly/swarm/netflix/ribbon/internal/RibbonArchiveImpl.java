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
package org.wildfly.swarm.netflix.ribbon.internal;

import java.io.IOException;

import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.wildfly.swarm.netflix.ribbon.RibbonArchive;
import org.wildfly.swarm.topology.internal.TopologyArchiveImpl;

/**
 * @author Bob McWhirter
 */
public class RibbonArchiveImpl extends TopologyArchiveImpl implements RibbonArchive {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public RibbonArchiveImpl(ArchiveBase<?> archive) throws IOException {
        super(archive);
    }

    @Override
    public RibbonArchive advertise() {
        return (RibbonArchive) super.advertise();
    }

    @Override
    public RibbonArchive advertise(String serviceName) {
        return (RibbonArchive) super.advertise(serviceName);
    }
}
