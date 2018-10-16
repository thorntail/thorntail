/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wildfly.swarm.plugin.gradle;

import java.io.File;
import java.util.Objects;
import java.util.StringJoiner;

import org.wildfly.swarm.tools.ArtifactSpec;

/**
 * The default implementation of {@link DependencyDescriptor}.
 */
public class DefaultDependencyDescriptor implements DependencyDescriptor {

    private final String scope;

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String type;

    private final String classifier;

    private final File file;

    // Transient members that are not serialized over the wire.
    private transient ArtifactSpec spec;

    /**
     * Construct a new instance of {@code DefaultDependencyDescriptor}.
     */
    public DefaultDependencyDescriptor(String scope, String groupId, String artifactId, String version, String type,
                                       String classifier, File file) {
        this.scope = scope;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.file = file;
    }

    /**
     * Construct a new instance of {@code DefaultDependencyDescriptor} from the given {@link ArtifactSpec} reference.
     */
    public DefaultDependencyDescriptor(ArtifactSpec spec) {
        this.scope = spec.scope;
        this.groupId = spec.groupId();
        this.artifactId = spec.artifactId();
        this.version = spec.version();
        this.type = spec.type();
        this.classifier = spec.classifier();
        this.file = spec.file;
        this.spec = spec;
    }

    @Override
    public String getScope() {
        return scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroup() {
        return groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return artifactId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassifier() {
        return classifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactSpec toArtifactSpec() {
        if (spec == null) {
            spec = new ArtifactSpec(getScope(), getGroup(), getName(), getVersion(), getType(), getClassifier(), getFile());
        }
        return spec;
    }

    /**
     * String representation of this descriptor.
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(":");
        joiner.add(groupId).add(artifactId).add(classifier == null ? "" : classifier).add(type).add(version);
        return "[" + scope + "] " + joiner.toString();
    }

    ///
    /// Equals & hashcode
    ///

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultDependencyDescriptor)) {
            return false;
        }
        DefaultDependencyDescriptor that = (DefaultDependencyDescriptor) o;
        return Objects.equals(scope, that.scope) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(type, that.type) &&
                Objects.equals(classifier, that.classifier) &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, groupId, artifactId, version, type, classifier, file);
    }
}
