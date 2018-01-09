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
package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.performance.Performance;

/**
 * @author Bob McWhirter
 */
public class MultiMavenResolver implements ArtifactResolver {


    public MultiMavenResolver() {

    }

    public void addResolver(ArtifactResolver resolver) {
        this.resolvers.add(resolver);
    }

    public ArtifactResolution resolveArtifact(ArtifactCoordinates coordinates, String packaging) throws IOException {

        try (AutoCloseable handle = Performance.accumulate("artifact-resolver")) {
            for (ArtifactResolver resolver : this.resolvers) {
                ArtifactResolution result = resolver.resolveArtifact(coordinates, packaging);
                if (result != null) {
                    return result;
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<ArtifactResolver> resolvers = new ArrayList<>();
}
