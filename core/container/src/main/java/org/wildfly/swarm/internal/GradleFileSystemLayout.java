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
package org.wildfly.swarm.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The {@code GradleFileSystemLayout} provides an abstraction over the file system layout for Gradle based projects. This class
 * is primarily used when executing tests, etc, from within the IDE / Gradle tasks.
 *
 * @author Heiko Braun
 * @since 02/08/16
 */
public class GradleFileSystemLayout extends FileSystemLayout {

    /**
     * Constant for specifying the Gradle project's build directory via the System properties.
     */
    public static final String SYSTEM_PROPERTY_BUILD_DIR = "build.dir";

    /**
     * Constant for specifying the Gradle project's sources root directory (under which the tooling can find find 'main'
     * source set).
     */
    public static final String SYSTEM_PROPERTY_SRC_DIR = "src.dir";

    /**
     * The name of the sources root directory.
     */
    public static final String SRC_DIR_NAME = "src";

    /**
     * The name of the build root directory.
     */
    public static final String BUILD_DIR_NAME = "build";

    private final Path rootPath;

    private final Path sourceRoot;

    private final Path buildRoot;

    private final Path classesDir;

    GradleFileSystemLayout(String root) {
        this.rootPath = Paths.get(root);

        // 1. Determine the sources root.
        //      - See if the developer has overridden the path.
        String value = System.getProperty(SYSTEM_PROPERTY_SRC_DIR, SRC_DIR_NAME);
        sourceRoot = rootPath.resolve(Paths.get(value));

        // 2. Determine the build root.
        //      - See if the developer has overridden the path.
        value = System.getProperty(SYSTEM_PROPERTY_BUILD_DIR, BUILD_DIR_NAME);
        buildRoot = rootPath.resolve(Paths.get(value));

        // Gradle 4 and above changed the build layout model. It now uses separate output directories for each jvm language.
        // First check if the layout corresponds to Gradle 4+ or not.
        Path path = buildRoot.resolve(CLASSES).resolve(JAVA).resolve(SOURCE_SET_MAIN);
        if (Files.exists(path)) {
            classesDir = path;
        } else {
            classesDir = buildRoot.resolve(CLASSES).resolve(SOURCE_SET_MAIN);
        }
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
        return classesDir;
    }

    @Override
    public Path resolveBuildResourcesDir() {
        return buildRoot.resolve(RESOURCES).resolve(SOURCE_SET_MAIN);
    }

    @Override
    public Path resolveSrcWebAppDir() {
        return sourceRoot.resolve(SOURCE_SET_MAIN).resolve(WEBAPP);
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }

    /**
     * Get the directory under which all built artifacts can be found.
     *
     * @return the directory under which all built artifacts can be found.
     */
    protected Path getBuildRoot() {
        return buildRoot;
    }

    protected static final String CLASSES = "classes";

    protected static final String SOURCE_SET_MAIN = "main";

    protected static final String RESOURCES = "resources";

    protected static final String WEBAPP = "webapp";

    private static final String JAVA = "java";

}
