/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Build tool filesystem abstraction for use in IDE:run cases.
 *
 * @author Heiko Braun
 * @since 02/08/16
 */
public abstract class FileSystemLayout {

    public abstract String determinePackagingType();

    public abstract Path resolveBuildClassesDir();

    public abstract Path resolveBuildResourcesDir();

    public abstract Path resolveSrcWebAppDir();

    public static String archiveNameForClassesDir(Path path) {

        if (path.endsWith(TARGET_CLASSES)) {
            // Maven
            return path.subpath(path.getNameCount() - 3, path.getNameCount() - 2).toString() + JAR;
        } else if (path.endsWith(BUILD_CLASSES_MAIN) || path.endsWith(BUILD_RESOURCES_MAIN)) {
            // Gradle + Gradle 4+
            return path.subpath(path.getNameCount() - 4, path.getNameCount() - 3).toString() + JAR;
        } else {
            return UUID.randomUUID().toString() + JAR;
        }
    }


    /**
     * Derived form 'user.dir'
     *
     * @return a FileSystemLayout instance
     */
    public static FileSystemLayout create() {

        String userDir = System.getProperty(USER_DIR);
        if (null == userDir) {
            throw SwarmMessages.MESSAGES.systemPropertyNotFound(USER_DIR);
        }

        return create(userDir);
    }

    /**
     * Derived from explicit path
     *
     * @param root the fs entry point
     * @return a FileSystemLayout instance
     */
    public static FileSystemLayout create(String root) {

        Path mavenBuildFile = MavenBuildFileResolver.resolveMavenBuildFileName(root);

        if (Files.exists(mavenBuildFile)) {
            return new MavenFileSystemLayout(mavenBuildFile);
        } else if (Files.exists(Paths.get(root, BUILD_GRADLE))) {
            return new GradleFileSystemLayout(root);
        }

        throw SwarmMessages.MESSAGES.cannotIdentifyFileSystemLayout(root);
    }



    public abstract Path getRootPath();

    private static final String BUILD_GRADLE = "build.gradle";

    private static final String USER_DIR = "user.dir";

    private static final String JAR = ".jar";

    private static final String TARGET_CLASSES = "target/classes";

    private static final String BUILD_CLASSES_MAIN = "build/classes/main";

    private static final String BUILD_CLASSES_JAVA_MAIN = "build/classes/java/main";

    private static final String BUILD_RESOURCES_MAIN = "build/resources/main";

    protected static final String TYPE_JAR = "jar";

    protected static final String TYPE_WAR = "war";

}
