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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.modules.maven.ArtifactCoordinates;
import org.jboss.modules.maven.MavenResolver;
import org.wildfly.swarm.bootstrap.performance.Performance;

/**
 * @author Bob McWhirter
 */
public class MultiMavenResolver implements MavenResolver, Closeable {


    public MultiMavenResolver() {

    }

    public void addResolver(MavenResolver resolver) {
        this.resolvers.add(resolver);
    }

    @Override
    public File resolveArtifact(ArtifactCoordinates coordinates, String packaging) throws IOException {

        try (AutoCloseable handle = Performance.accumulate("artifact-resolver")) {
            for (MavenResolver resolver : this.resolvers) {
                File result = resolver.resolveArtifact(coordinates, packaging);
                if (result != null) {
                    return result;
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        for (MavenResolver resolver : this.resolvers) {
            if (resolver instanceof Closeable) {
                Closeable closeable = (Closeable) resolver;
                closeable.close();
            }
        }
    }

    private List<MavenResolver> resolvers = new ArrayList<>();
}
