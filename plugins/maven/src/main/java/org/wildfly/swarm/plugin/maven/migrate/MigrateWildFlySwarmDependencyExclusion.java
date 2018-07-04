/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin.maven.migrate;

import org.joox.Match;

import java.nio.file.Path;
import java.util.Objects;

import static org.joox.JOOX.$;
import static org.wildfly.swarm.plugin.maven.migrate.MavenUtils.parsePomXml;

final class MigrateWildFlySwarmDependencyExclusion implements MigrationAction {
    private final Path pomXml;
    private final DependencyLocation location;
    private final String groupId;
    private final String artifactId;

    MigrateWildFlySwarmDependencyExclusion(Path pomXml, DependencyLocation location, String groupId, String artifactId) {
        this.pomXml = pomXml;
        this.location = location;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public String describe() {
        return pomXml + ": migrate dependency exclusion on " + groupId + ":" + artifactId + " at " + location;
    }

    @Override
    public void execute() throws Exception {
        Match pom = parsePomXml(pomXml);

        location.find(pom)
                .xpath("m:exclusions/m:exclusion")
                .filter(exclusion -> {
                    String groupId = $(exclusion).child("groupId").text();
                    String artifactId = $(exclusion).child("artifactId").text();
                    return this.groupId.equals(groupId) && this.artifactId.equals(artifactId);
                })
                .each(exclusion -> $(exclusion).child("groupId").text("io.thorntail"));

        MavenUtils.writePomXml(pomXml, pom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MigrateWildFlySwarmDependencyExclusion)) {
            return false;
        }
        MigrateWildFlySwarmDependencyExclusion that = (MigrateWildFlySwarmDependencyExclusion) o;
        return Objects.equals(pomXml, that.pomXml) &&
                Objects.equals(location, that.location) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pomXml, location, groupId, artifactId);
    }
}
