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

import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Archive mix-in capable of supporting {@code addAllDependencies}
 *
 * <p>While not used directly, this interface is exposed through sub-classes
 * that support adding all dependencies.</p>
 *
 * <p>It is a syntactic sugar for ShrinkWrap's own {@code addAsLibraries} along
 * with {@link ArtifactLookup#allArtifacts()}</p>
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 * @see ArtifactLookup
 */
public interface DependenciesContainer<T extends Archive<T>> extends LibraryContainer<T>, MarkerContainer<T>, Archive<T> {

    String ALL_DEPENDENCIES_MARKER = "org.wildfly.swarm.allDependencies";

    /**
     * Add all application dependencies to this deployment.
     *
     * @return this archive.
     */
    @SuppressWarnings("unchecked")
    default T addAllDependencies() throws Exception {
        // The actual dependencies are added in the runtime stage.
        // See RuntimeDeployer#deploy(...) for further details.

        // flag to instruct the container to add the missing deps upon deploy time
        addMarker(ALL_DEPENDENCIES_MARKER);
        return (T) this;
    }


    @SuppressWarnings("unchecked")
    // [hb] TODO: is this actually needed anymore? looks brittle to add swarm deps tp the deployment ...
    default T addAllDependencies(boolean includingWildFlySwarm) throws Exception {
        if (!includingWildFlySwarm) {
            return addAllDependencies();
        }

        if (!hasMarker(ALL_DEPENDENCIES_MARKER)) {
            List<JavaArchive> artifacts = ArtifactLookup.get().allArtifacts();
            addAsLibraries(artifacts);
            addMarker(ALL_DEPENDENCIES_MARKER);
        }

        return (T) this;


    }

    /**
     * Add a single Maven dependency into the Archive.
     * The following dependency formats are supported:
     *
     * groupId:artifactId
     * groupId:artifactId:version
     * groupId:artifactId:packaging:version
     * groupId:artifactId:packaging:version:classifier
     *
     * @param gav String coordinates of the Maven dependency
     * @return Archive instance
     * @throws Exception
     * @see ArtifactLookup#artifact(String)
     */
    @SuppressWarnings("unchecked")
    default T addDependency(String gav) throws Exception {
        addAsLibrary(ArtifactLookup.get().artifact(gav));
        return (T) this;
    }
}
