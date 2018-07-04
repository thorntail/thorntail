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

enum DependencyLocationType {
    // <plugin>/dependencyManagement/dependencies/dependency
    DEPENDENCY_MANAGEMENT("dependencyManagement"),
    // <plugin>/dependencies/dependency
    DEPENDENCIES("dependency");

    private final String description;

    DependencyLocationType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
