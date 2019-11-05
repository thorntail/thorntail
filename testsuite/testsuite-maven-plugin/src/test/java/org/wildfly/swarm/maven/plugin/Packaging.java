/**
 * Copyright 2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.maven.plugin;

/**
 * How should the testing project be packaged. This is essentially equivalent to Maven project packaging.
 * (If we tested custom {@code main} methods, we'd have a separate value for with and without custom {@code main},
 * and so this wouldn't be the same as Maven packaging. Custom {@code main} methods, however, are no longer supported.)
 */
public enum Packaging {
    WAR("war"),
    JAR("jar");

    private final String value;

    Packaging(String value) {
        this.value = value;
    }

    public String packagingType() {
        return value;
    }

    public String fileExtension() {
        return value;
    }
}
