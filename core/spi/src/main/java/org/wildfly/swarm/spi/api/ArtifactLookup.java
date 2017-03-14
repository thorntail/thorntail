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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Manager capable of locating artifacts from the application build.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface ArtifactLookup {

    AtomicReference<ArtifactLookup> INSTANCE = new AtomicReference<>();

    static ArtifactLookup get() {
        return INSTANCE.updateAndGet((e) -> {
            if (e != null) {
                return e;
            }

            try {
                return (ArtifactLookup) Class.forName("org.wildfly.swarm.internal.ArtifactManager").newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
                // TODO handle error
            }
            return null;
        });
    }

    /**
     * Retrieve an artifact that was part of the original build using a
     * full or simplified Maven GAV specifier.
     *
     * <p>The following formats of GAVs are supported:</p>
     *
     * <ul>
     * <li>groupId:artifactId</li>
     * <li>groupId:artifactId:version</li>
     * <li>groupId:artifactId:packaging:version</li>
     * <li>groupId:artifactId:packaging:version:classifier</li>
     * </ul>
     *
     * <p>Only artifacts that were compiled with the user's project with
     * a scope of {@code compile} are available through lookup.</p>
     *
     * <p>In the variants that include a {@code version} parameter, it may be
     * replaced by a literal asterisk in order to avoid hard-coding versions
     * into the application.</p>
     *
     * @param gav The Maven GAV.
     * @return The located artifact, as a {@code JavaArchive}.
     * @throws Exception if an error occurs locating or loading the artifact.
     */
    JavaArchive artifact(String gav) throws Exception;

    /**
     * Retrieve an artifact that was part of the original build using a
     * full or simplified Maven GAV specifier, returning an archive with a
     * specified name.
     *
     * @param gav The Maven GAV.
     * @return The located artifact, as a {@code JavaArchive} with the specified name.
     * @throws Exception if an error occurs locating or loading the artifact.
     * @see #artifact(String)
     */
    JavaArchive artifact(String gav, String asName) throws Exception;

    /**
     * Retrieve all dependency artifacts for the user's project.
     *
     * @return All dependencies, as {@code JavaArchive} objects.
     * @throws Exception if an error occurs locating or loading any artifact.
     */
    List<JavaArchive> allArtifacts() throws Exception;

    List<JavaArchive> allArtifacts(String... groupIdExclusions) throws Exception;
}
