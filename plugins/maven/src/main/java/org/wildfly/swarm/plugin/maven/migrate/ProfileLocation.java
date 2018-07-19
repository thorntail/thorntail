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

import java.util.Objects;

import static org.joox.JOOX.$;

final class ProfileLocation {
    private final ProfileLocationType type;

    private final String id;

    static ProfileLocation none() {
        return new ProfileLocation(ProfileLocationType.NONE, "<no profile>");
    }

    static ProfileLocation profile(Match profile) {
        return new ProfileLocation(ProfileLocationType.PROFILE, profile.child("id").text());
    }

    private ProfileLocation(ProfileLocationType type, String id) {
        this.type = type;
        this.id = id;
    }

    Match find(Match pom) {
        switch (type) {
            case NONE:
                return pom;
            case PROFILE:
                return pom.xpath("m:profiles/m:profile")
                        .filter(profile -> {
                            String id = $(profile).child("id").text();
                            return this.id.equals(id);
                        });
            default:
                throw new AssertionError();
        }
    }

    boolean isNone() {
        return type == ProfileLocationType.NONE;
    }

    @Override
    public String toString() {
        if (type == ProfileLocationType.NONE) {
            return "";
        } else {
            return type + " " + id;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfileLocation)) {
            return false;
        }
        ProfileLocation that = (ProfileLocation) o;
        return type == that.type &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
