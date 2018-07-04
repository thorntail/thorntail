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

final class PluginLocation {
    private final ProfileLocation profileLocation;

    private final PluginLocationType type;

    private final String groupId;
    private final String artifactId;

    static PluginLocation none(ProfileLocation profileLocation) {
        return new PluginLocation(profileLocation, PluginLocationType.NONE, null);
    }

    static PluginLocation pluginManagement(ProfileLocation profileLocation, Match plugin) {
        return new PluginLocation(profileLocation, PluginLocationType.PLUGIN_MANAGEMENT, plugin);
    }

    static PluginLocation plugins(ProfileLocation profileLocation, Match plugin) {
        return new PluginLocation(profileLocation, PluginLocationType.PLUGINS, plugin);
    }

    private PluginLocation(ProfileLocation profileLocation, PluginLocationType type, Match plugin) {
        this.profileLocation = profileLocation;
        this.type = type;
        if (plugin != null) {
            this.groupId = plugin.child("groupId").text();
            this.artifactId = plugin.child("artifactId").text();
        } else {
            this.groupId = null;
            this.artifactId = null;
        }
    }

    Match find(Match pom) {
        Match profile = profileLocation.find(pom);

        Filter pluginFilter = plugin -> {
            String groupId = $(plugin).child("groupId").text();
            String artifactId = $(plugin).child("artifactId").text();
            return this.groupId.equals(groupId) && this.artifactId.equals(artifactId);
        };

        switch (type) {
            case NONE:
                return profile;
            case PLUGIN_MANAGEMENT:
                return profile.xpath("m:build/m:pluginManagement/m:plugins/m:plugin")
                        .filter(pluginFilter);
            case PLUGINS:
                return profile.xpath("m:build/m:plugins/m:plugin")
                        .filter(pluginFilter);
            default:
                throw new AssertionError();
        }
    }

    boolean isNone() {
        return type == PluginLocationType.NONE;
    }

    @Override
    public String toString() {
        String base = "";
        if (type != PluginLocationType.NONE) {
            base = type + " " + groupId + ":" + artifactId;
        }

        return base + (profileLocation.isNone() ? "" : " in " + profileLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginLocation)) {
            return false;
        }
        PluginLocation that = (PluginLocation) o;
        return Objects.equals(profileLocation, that.profileLocation) &&
                type == that.type &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileLocation, type, groupId, artifactId);
    }
}
