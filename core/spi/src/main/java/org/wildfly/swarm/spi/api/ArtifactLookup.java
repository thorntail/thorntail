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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
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
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            return null;
        });
    }

    Archive artifact(String gav) throws Exception;

    Archive artifact(String gav, String asName) throws Exception;

    List<JavaArchive> allArtifacts() throws Exception;

    List<JavaArchive> allArtifacts(String... groupIdExclusions) throws Exception;
}
