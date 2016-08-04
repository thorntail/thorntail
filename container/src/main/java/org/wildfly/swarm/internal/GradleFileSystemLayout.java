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
        if(resolveSrcWebAppDir().toFile().exists()) {
            return TYPE_WAR;
        }
        else {
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

    private static final String BUILD = "build";

    private static final String CLASSES = "classes";

    private static final String MAIN = "main";

    private static final String RESOURCES = "resources";

    private static final String SRC = "src";

    private static final String WEBAPP = "webapp";

    private final Path rootPath;
}
