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
package org.wildfly.swarm.spi.api;

/** Hook point to allow user-space (deployment) extensions for CDIi runtimes.
 *
 * @apiNote Typically advanced usage by the core.
 *
 * @author Bob McWhirter
 */
public interface UserSpaceExtensionFactory {

    /** Perform whatever is necessary to configure the user-space factory.
     *
     * @throws Exception
     */
    void configure() throws Exception;
}
