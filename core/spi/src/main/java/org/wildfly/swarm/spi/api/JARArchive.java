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
package org.wildfly.swarm.spi.api;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;

/** An improvement of the ShrinkWrap {@code JavaArchive}.
 *
 * <p>Like the {@code JavaArchive}, this archive type supports adding
 * classes and resources.  Additionally, it supports smarter addition
 * of service-providers to be discovered through the JDK {@code ServiceLoader}
 * patterns.  Additionally, it supports adding modules as though through
 * {@code jboss-deployment-structure.xml}.</p>
 *
 * @see ServiceProviderContainer
 * @see JBossDeploymentStructureContainer
 *
 * @author Bob McWhirter
 */
public interface JARArchive extends
        Archive<JARArchive>,
        ServiceProviderContainer<JARArchive>,
        //DependenciesContainer<JARArchive>,
        JBossDeploymentStructureContainer<JARArchive> {

}
