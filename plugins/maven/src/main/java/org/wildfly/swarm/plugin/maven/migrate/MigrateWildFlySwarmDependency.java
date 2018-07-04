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

final class MigrateWildFlySwarmDependency implements MigrationAction {
    private final Path pomXml;
    private final DependencyLocation location;
    // null = version shouldn't be changed (generally because it uses a property)
    private final String targetVersion;

    MigrateWildFlySwarmDependency(Path pomXml, DependencyLocation location, String targetVersion) {
        this.pomXml = pomXml;
        this.location = location;
        this.targetVersion = targetVersion;
    }

    @Override
    public String describe() {
        return pomXml + ": migrate " + location + " (target version: " + targetVersion + ")";
    }

    @Override
    public void execute() throws Exception {
        Match pom = parsePomXml(pomXml);

        location.find(pom).each(dependency -> {
            $(dependency).child("groupId").text("io.thorntail");
            if (targetVersion != null) {
                $(dependency).child("version").text(targetVersion);
            }
        });

        MavenUtils.writePomXml(pomXml, pom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MigrateWildFlySwarmDependency)) {
            return false;
        }
        MigrateWildFlySwarmDependency that = (MigrateWildFlySwarmDependency) o;
        return Objects.equals(pomXml, that.pomXml) &&
                Objects.equals(location, that.location) &&
                Objects.equals(targetVersion, that.targetVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pomXml, location, targetVersion);
    }
}
