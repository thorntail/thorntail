/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import java.io.File;

import org.wildfly.swarm.bootstrap.util.MavenArtifactDescriptor;

/**
 * @author Bob McWhirter
 */
public class ArtifactSpec extends MavenArtifactDescriptor {

    public final String scope;
    public File file;

    public boolean shouldGather = true;
    public boolean gathered = false;

    public ArtifactSpec(String scope, String groupId, String artifactId, String version, String packaging, String classifier, File file) {
        super( groupId, artifactId, packaging, classifier, version );
        this.scope = scope;
        this.file = file;
    }

    public String toString() {
        return mavenGav() + " [" + this.scope + "]";
    }
}
