/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.internal;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Heiko Braun
 * @since 02/08/16
 */
public class GradleFileSystemLayout extends FileSystemLayout {

    GradleFileSystemLayout(String root) {
        this.rootPath = Paths.get(root);
    }

    @Override
    public String determinePackagingType() {
        if (resolveSrcWebAppDir().toFile().exists()) {
            return TYPE_WAR;
        } else {
            return TYPE_JAR;
        }
    }

    @Override
    public Path resolveBuildClassesDir() {
        return rootPath.resolve(BUILD).resolve(CLASSES).resolve(MAIN);
    }

    @Override
    public Path resolveBuildResourcesDir() {
        return rootPath.resolve(BUILD).resolve(RESOURCES).resolve(MAIN);
    }

    @Override
    public Path resolveSrcWebAppDir() {
        return rootPath.resolve(SRC).resolve(MAIN).resolve(WEBAPP);
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }

    private static final String BUILD = "build";

    private static final String CLASSES = "classes";

    private static final String MAIN = "main";

    private static final String RESOURCES = "resources";

    private static final String SRC = "src";

    private static final String WEBAPP = "webapp";

    private final Path rootPath;
}
