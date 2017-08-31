/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.container.internal;

import java.nio.file.Path;
import java.util.Collection;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.container.DeploymentException;

/**
 * @author Bob McWhirter
 */
public interface Deployer {
    void deploy() throws DeploymentException;

    void deploy(Archive<?> deployment) throws DeploymentException;

    void deploy(Collection<Path> paths) throws DeploymentException;

    Archive<?> createDefaultDeployment();
}
