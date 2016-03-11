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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class FractionDescriptor {

    public FractionDescriptor(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void addDependency(FractionDescriptor dep) {
        this.dependencies.add(dep);
    }

    public String groupId() {
        return this.groupId;
    }

    public String artifactId() {
        return this.artifactId;
    }

    public String version() {
        return this.version;
    }

    public Set<FractionDescriptor> getDependencies() {
        return Collections.unmodifiableSet(this.dependencies);
    }

    public ArtifactSpec toArtifactSpec() {
        return ArtifactSpec.fromMscGav(toString());
    }

    public String av() {
        return this.artifactId + ":" + this.version;
    }

    public String gav() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }

    public String gavOrAv() {
        if (DependencyManager.WILDFLY_SWARM_GROUP_ID.equals(this.groupId)) {

            return av();
        }

        return gav();
    }

    @Override
    public String toString() {
        return gav();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        FractionDescriptor other = (FractionDescriptor) obj;
        if (artifactId == null) {
            if (other.artifactId != null)
                return false;
        } else if (!artifactId.equals(other.artifactId))
            return false;

        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;

        return true;
    }

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final Set<FractionDescriptor> dependencies = new HashSet<>();
}
