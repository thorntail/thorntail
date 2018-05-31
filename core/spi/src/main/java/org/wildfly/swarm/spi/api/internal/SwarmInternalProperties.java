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
package org.wildfly.swarm.spi.api.internal;

/**
 * Defines a set of properties used internally by Thorntail.
 *
 * @author Ken Finnigan
 */
public interface SwarmInternalProperties {

    String BUILD_MODULES = "swarm.build.modules";

    String BUILD_REPOS = "swarm.build.repos";

    String EXPORT_UBERJAR = "swarm.export.uberjar";

    String CURRENT_DEPLOYMENT = "swarm.current.deployment";

    String NODE_ID = "swarm.node.id";

}
