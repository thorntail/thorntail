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
package org.wildfly.swarm.tools;

import java.util.Collection;
import java.util.Set;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface ArtifactResolvingHelper {
    /**
     * Resolves the file for given artifact.
     *
     * @param spec The artifact to resolve
     * @return spec with the file property set, or null if no file found
     * @throws Exception
     */
    ArtifactSpec resolve(ArtifactSpec spec) throws Exception;

    default Set<ArtifactSpec> resolveAll(Collection<ArtifactSpec> specs) throws Exception {
        return resolveAll(specs, true, false);
    }

    Set<ArtifactSpec> resolveAll(Collection<ArtifactSpec> specs, boolean transitive, boolean defaultExcludes) throws Exception;
}
