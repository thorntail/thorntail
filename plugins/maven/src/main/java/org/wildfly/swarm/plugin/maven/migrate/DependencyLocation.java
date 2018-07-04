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

import org.joox.Filter;
import org.joox.Match;

import java.util.Objects;

import static org.joox.JOOX.$;

final class DependencyLocation {
    private final PluginLocation pluginLocation;

    private final DependencyLocationType type;

    private final String groupId;
    private final String artifactId;

    static DependencyLocation dependencyManagement(PluginLocation pluginLocation, Match dependency) {
        return new DependencyLocation(pluginLocation, DependencyLocationType.DEPENDENCY_MANAGEMENT, dependency);
    }

    static DependencyLocation dependencies(PluginLocation pluginLocation, Match dependency) {
        return new DependencyLocation(pluginLocation, DependencyLocationType.DEPENDENCIES, dependency);
    }

    private DependencyLocation(PluginLocation pluginLocation, DependencyLocationType type, Match dependency) {
        this.pluginLocation = pluginLocation;
        this.type = type;
        this.groupId = dependency.child("groupId").text();
        this.artifactId = dependency.child("artifactId").text();
    }

    Match find(Match pom) {
        Match plugin = pluginLocation.find(pom);

        Filter dependencyFilter = dependency -> {
            String groupId = $(dependency).child("groupId").text();
            String artifactId = $(dependency).child("artifactId").text();
            return this.groupId.equals(groupId) && this.artifactId.equals(artifactId);
        };

        switch (type) {
            case DEPENDENCY_MANAGEMENT:
                return plugin.xpath("m:dependencyManagement/m:dependencies/m:dependency")
                        .filter(dependencyFilter);
            case DEPENDENCIES:
                return plugin.xpath("m:dependencies/m:dependency")
                        .filter(dependencyFilter);
            default:
                throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return type + " " + groupId + ":" + artifactId + (pluginLocation.isNone() ? "" : " of ") + pluginLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DependencyLocation)) {
            return false;
        }
        DependencyLocation that = (DependencyLocation) o;
        return Objects.equals(pluginLocation, that.pluginLocation) &&
                type == that.type &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginLocation, type, groupId, artifactId);
    }
}
