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
package org.jboss.modules.maven;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maven artifact coordinate specification.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ArtifactCoordinates {

    static final Pattern snapshotPattern = Pattern.compile("-\\d{8}\\.\\d{6}-\\d+$");
    private static final Pattern VALID_PATTERN = Pattern.compile("^([-_a-zA-Z0-9.]+):([-_a-zA-Z0-9.]+):([-_a-zA-Z0-9.]+)(?::([-_a-zA-Z0-9.]+))?$");

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private Boolean isSnapshot = null;
    private int hashCode;
    private String toString;

    /**
     * Construct a new instance.
     *
     * @param groupId the group ID (must not be {@code null})
     * @param artifactId the artifact ID (must not be {@code null})
     * @param version the version string (must not be {@code null})
     * @param classifier the classifier string (must not be {@code null}, may be empty)
     */
    public ArtifactCoordinates(final String groupId, final String artifactId, final String version, final String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }

    /**
     * Construct a new instance with an empty classifier.
     *
     * @param groupId the group ID (must not be {@code null})
     * @param artifactId the artifact ID (must not be {@code null})
     * @param version the version string (must not be {@code null})
     */
    public ArtifactCoordinates(final String groupId, final String artifactId, final String version) {
        this(groupId, artifactId, version, "");
    }

    /**
     * Parse a string and produce artifact coordinates from it.
     *
     * @param string the string to parse (must not be {@code null})
     * @return the artifact coordinates object (not {@code null})
     */
    public static ArtifactCoordinates fromString(String string) {
        final Matcher matcher = VALID_PATTERN.matcher(string);
        if (matcher.matches()) {
            if (matcher.group(4) != null) {
                return new ArtifactCoordinates(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            } else {
                return new ArtifactCoordinates(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        } else {
            throw new IllegalArgumentException(string);
        }
    }

    /**
     * Get the group ID.
     *
     * @return the group ID (not {@code null})
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get the artifact ID.
     *
     * @return the artifact ID (not {@code null})
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the version.
     *
     * @return the version (not {@code null})
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the classifier.
     *
     * @return the classifier (not {@code null}, may be empty)
     */
    public String getClassifier() {
        return classifier;
    }

    public boolean isSnapshot() {
        if (isSnapshot == null) {
            String version1 = getVersion();
            final Matcher versionMatcher = snapshotPattern.matcher(version1);
            if (versionMatcher.find()) {
                isSnapshot = Boolean.TRUE;
            } else {
                isSnapshot = version1.contains("-SNAPSHOT");
            }
        }
        return isSnapshot;
    }

    /**
     * Create a relative repository path for the given artifact coordinates.
     *
     * @param separator the separator character to use (typically {@code '/'} or {@link File#separatorChar})
     * @return the path string
     */
    public String relativeArtifactPath(char separator) {
        String artifactId1 = getArtifactId();
        String version1 = getVersion();
        StringBuilder builder = new StringBuilder(getGroupId().replace('.', separator));
        builder.append(separator).append(artifactId1).append(separator);
        String pathVersion;
        final Matcher versionMatcher = snapshotPattern.matcher(version1);
        if (versionMatcher.find()) {
            // it's really a snapshot
            pathVersion = version1.substring(0, versionMatcher.start()) + "-SNAPSHOT";
        } else {
            pathVersion = version1;
        }
        builder.append(pathVersion).append(separator).append(artifactId1).append('-').append(version1);
        return builder.toString();
    }

    /**
     * Create a relative repository path for the given artifact coordinates with a {@code '/'} separator.
     *
     * @return the path string
     */
    public String relativeArtifactPath() {
        return relativeArtifactPath('/');
    }

    public String relativeArtifactPath(char separator, String timestampVersion) {
        String artifactId1 = getArtifactId();
        String version1 = getVersion();
        StringBuilder builder = new StringBuilder(getGroupId().replace('.', separator));
        builder.append(separator).append(artifactId1).append(separator);
        String pathVersion;
        final Matcher versionMatcher = snapshotPattern.matcher(version1);
        if (versionMatcher.find()) {
            // it's really a snapshot
            pathVersion = version1.substring(0, versionMatcher.start()) + "-SNAPSHOT";
        } else {
            pathVersion = version1;
        }
        builder.append(pathVersion).append(separator).append(artifactId1).append('-').append(timestampVersion);
        return builder.toString();
    }

    public String relativeMetadataPath(char separator) {
        String artifactId1 = getArtifactId();
        String version1 = getVersion();
        StringBuilder builder = new StringBuilder(getGroupId().replace('.', separator));
        builder.append(separator).append(artifactId1).append(separator);
        String pathVersion;
        final Matcher versionMatcher = snapshotPattern.matcher(version1);
        if (versionMatcher.find()) {
            // it's really a snapshot
            pathVersion = version1.substring(0, versionMatcher.start()) + "-SNAPSHOT";
        } else {
            pathVersion = version1;
        }
        builder.append(pathVersion).append(separator).append("maven-metadata.xml");
        return builder.toString();
    }

    /**
     * Determine whether this coordinates object equals the target object.
     *
     * @param obj the target object
     * @return {@code true} if the object is equal to this one, {@code false} otherwise
     */
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof ArtifactCoordinates && equals((ArtifactCoordinates) obj);
    }

    /**
     * Determine whether this coordinates object equals the target object.
     *
     * @param obj the target object
     * @return {@code true} if the object is equal to this one, {@code false} otherwise
     */
    public boolean equals(final ArtifactCoordinates obj) {
        return this == obj || obj != null && groupId.equals(obj.groupId) && artifactId.equals(obj.artifactId) && version.equals(obj.version) && classifier.equals(obj.classifier);
    }

    /**
     * Get the hash code.
     *
     * @return the hash code
     */
    public int hashCode() {
        int hashCode = this.hashCode;
        if (hashCode == 0) {
            hashCode = ((groupId.hashCode() * 19 + artifactId.hashCode()) * 19 + version.hashCode()) * 19 + classifier.hashCode();
            if (hashCode == 0) {
                hashCode = -1;
            }
            this.hashCode = hashCode;
        }
        return hashCode;
    }

    /**
     * Get the string representation.
     *
     * @return the string representation
     */
    public String toString() {
        String toString = this.toString;
        if (toString == null) {
            final StringBuilder b = new StringBuilder(groupId.length() + artifactId.length() + version.length() + classifier.length() + 16);
            b.append(groupId).append(':').append(artifactId).append(':').append(version);
            if (! classifier.isEmpty()) {
                b.append(':').append(classifier);
            }
            this.toString = toString = b.toString();
        }
        return toString;
    }
}
