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
package org.wildfly.swarm.fractions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class FractionDescriptor {

    public static final String THORNTAIL_GROUP_ID = "io.thorntail";

    public FractionDescriptor(String groupId, String artifactId, String version, String name, String description, String tags, boolean internal, FractionStability stability) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.internal = internal;
        this.stability = stability;
    }

    public FractionDescriptor(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, null, null, false, FractionStability.UNSTABLE);
    }

    /**
     * Retrieves a {@link FractionDescriptor} from the {@code fractionList} based on the {@code gav} string.
     *
     * <p>The {@code gav} string contains colon-delimited Maven artifact coordinates. Supported formats are:</p>
     *
     * <ul>
     * <li>{@code artifactId}: the groupId {@code org.wildfly.swarm} is presumed</li>
     * <li>{@code artifactId:version}: the groupId {@code org.wildfly.swarm} is presumed</li>
     * <li>{@code groupId:artifactId:version}</li>
     * </ul>
     *
     * <p>If the {@code fractionList} doesn't contain such fraction, an exception is thrown.</p>
     */
    public static FractionDescriptor fromGav(final FractionList fractionList, final String gav) {
        final String[] parts = gav.split(":");
        FractionDescriptor desc = null;

        switch (parts.length) {
            case 1:
                desc = fractionList.getFractionDescriptor(THORNTAIL_GROUP_ID, parts[0]);
                if (desc == null) {
                    throw new RuntimeException("Fraction not found: " + gav);
                }
                break;
            case 2:
                desc = fractionList.getFractionDescriptor(THORNTAIL_GROUP_ID, parts[0]);
                if (desc == null) {
                    throw new RuntimeException("Fraction not found: " + gav);
                }
                if (!desc.getVersion().equals(parts[1])) {
                    throw new RuntimeException("Version mismatch: requested " + gav + ", found " + desc.av());
                }
                break;
            case 3:
                desc = fractionList.getFractionDescriptor(parts[0], parts[1]);
                if (desc == null) {
                    throw new RuntimeException("Fraction not found: " + gav);
                }
                if (!desc.getVersion().equals(parts[2])) {
                    throw new RuntimeException("Version mismatch: requested " + gav + ", found " + desc.gav());
                }
                break;
            default:
                throw new RuntimeException("Invalid fraction spec: " + gav);
        }

        return desc;
    }

    public void addDependency(FractionDescriptor dep) {
        this.dependencies.add(dep);
    }

    /**
     * @deprecated Replaced with {@link #getGroupId()}
     */
    @Deprecated
    public String groupId() {
        return this.groupId;
    }

    /**
     * @deprecated Replaced with {@link #getArtifactId()}
     */
    @Deprecated
    public String artifactId() {
        return this.artifactId;
    }

    /**
     * @deprecated Replaced with {@link #getVersion()}
     */
    @Deprecated
    public String version() {
        return this.version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public boolean isInternal() {
        return internal;
    }

    public FractionStability getStability() {
        return stability;
    }

    public Set<FractionDescriptor> getDependencies() {
        return Collections.unmodifiableSet(this.dependencies);
    }

    public String av() {
        return this.artifactId + ":" + this.version;
    }

    public String gav() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }

    public String gavOrAv() {
        if (THORNTAIL_GROUP_ID.equals(this.groupId)) {

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
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        FractionDescriptor other = (FractionDescriptor) obj;
        if (artifactId == null) {
            if (other.artifactId != null) {
                return false;
            }
        } else if (!artifactId.equals(other.artifactId)) {
            return false;
        }

        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }

        return true;
    }

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String name;

    private final String description;

    private final String tags;

    private final boolean internal;

    private final Set<FractionDescriptor> dependencies = new HashSet<>();

    private final FractionStability stability;
}
